/**
 * Copyright (c) 2024, Openflexo
 *
 * This file is part of Pamela-core, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html.
 */

package org.openflexo.pamela.sync;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RabbitMQ-based synchronization manager for PAMELA collaborative editing.
 * Handles publishing operations to the message broker and consuming operations from other replicas.
 * 
 * @author PAMELA Team
 */
public class RabbitMQSyncManager implements SyncManager, AutoCloseable {

	private static final Logger logger = Logger.getLogger(RabbitMQSyncManager.class.getName());

	// RabbitMQ connection settings
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	private final String virtualHost;
	private final boolean useSsl;

	// Exchange and queue configuration
	private final String exchangeName;
	private final String routingKey;

	// Unique identifier for this replica
	private final String replicaId;

	// Vector clock for this replica
	private final VectorClock vectorClock;

	// RabbitMQ connection and channel
	private Connection connection;
	private Channel publishChannel;
	private Channel consumeChannel;
	private String queueName;

	// Listeners for received operations
	private final List<SyncOperationListener> listeners = new CopyOnWriteArrayList<>();

	// Connection state
	private volatile boolean connected = false;
	private volatile boolean closing = false;

	/**
	 * Create a RabbitMQ sync manager with default settings
	 */
	public RabbitMQSyncManager() {
		this("localhost", 5672, "guest", "guest", "/", "pamela.sync", "operations", false);
	}

	/**
	 * Create a RabbitMQ sync manager with custom settings
	 */
	public RabbitMQSyncManager(String host, int port, String username, String password,
			String virtualHost, String exchangeName, String routingKey, boolean useSsl) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.virtualHost = virtualHost;
		this.exchangeName = exchangeName;
		this.routingKey = routingKey;
		this.useSsl = useSsl;
		this.replicaId = UUID.randomUUID().toString();
		this.vectorClock = new VectorClock();
	}

	/**
	 * Get the unique replica identifier
	 */
	public String getReplicaId() {
		return replicaId;
	}

	/**
	 * Get the current vector clock
	 */
	public VectorClock getVectorClock() {
		return vectorClock;
	}

	/**
	 * Check if connected to RabbitMQ
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Connect to RabbitMQ and start consuming messages
	 */
	public void connect() throws IOException, TimeoutException {
		if (connected) {
			logger.warning("Already connected to RabbitMQ");
			return;
		}

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);

		// Enable SSL if configured
		if (useSsl) {
			try {
				factory.useSslProtocol();
			} catch (Exception e) {
				throw new IOException("Failed to enable SSL", e);
			}
		}

		// Enable automatic recovery
		factory.setAutomaticRecoveryEnabled(true);
		factory.setNetworkRecoveryInterval(5000);

		connection = factory.newConnection("PAMELA-" + replicaId.substring(0, 8));

		// Create channels for publishing and consuming
		publishChannel = connection.createChannel();
		consumeChannel = connection.createChannel();

		// Declare the fanout exchange for broadcast
		publishChannel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT, true);

		// Create an exclusive queue for this replica
		queueName = consumeChannel.queueDeclare().getQueue();
		consumeChannel.queueBind(queueName, exchangeName, routingKey);

		// Set up consumer
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			if (closing) return;

			try {
				SyncOperation operation = SyncOperationSerializer.deserializeFromBytes(delivery.getBody());

				// Ignore operations from this replica
				if (replicaId.equals(operation.getReplicaId())) {
					return;
				}

				// Handle STATE_REQUEST and STATE_RESPONSE specially
				if (operation.getOperationType() == SyncOperation.OperationType.STATE_REQUEST) {
					// Another replica is requesting state
					notifyStateRequested(operation.getReplicaId());
					return;
				}

				if (operation.getOperationType() == SyncOperation.OperationType.STATE_RESPONSE) {
					// Check if this response is for us (targetReplicaId stored in propertyIdentifier)
					String targetReplicaId = operation.getPropertyIdentifier();
					if (targetReplicaId == null || targetReplicaId.equals(replicaId)) {
						notifyStateReceived(operation.getNewValueSerialized(), operation.getReplicaId());
					}
					return;
				}

				// Update our vector clock
				if (operation.getVectorClock() != null) {
					vectorClock.merge(operation.getVectorClock());
				}

				// Notify listeners
				for (SyncOperationListener listener : listeners) {
					try {
						listener.onOperationReceived(operation);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Error in operation listener", e);
					}
				}

			} catch (SyncOperationSerializer.SyncSerializationException e) {
				logger.log(Level.SEVERE, "Failed to deserialize operation", e);
				notifyError(e);
			}
		};

		CancelCallback cancelCallback = consumerTag -> {
			if (!closing) {
				logger.warning("Consumer cancelled: " + consumerTag);
				notifyDisconnected("Consumer cancelled");
			}
		};

		consumeChannel.basicConsume(queueName, true, deliverCallback, cancelCallback);

		connected = true;
		logger.info("Connected to RabbitMQ at " + host + ":" + port + " as replica " + replicaId);
		notifyConnected();
	}

	/**
	 * Publish a synchronization operation to all replicas
	 * 
	 * @param operation the operation to publish
	 */
	@Override
	public void publishOperation(SyncOperation operation) {
		if (!connected) {
			logger.warning("Cannot publish: not connected to RabbitMQ");
			return;
		}

		try {
			// Increment our vector clock
			vectorClock.increment(replicaId);

			// Create a new operation with the updated vector clock
			SyncOperation operationWithClock = new SyncOperation.Builder(operation.getOperationType())
					.operationId(operation.getOperationId())
					.timestamp(operation.getTimestamp())
					.replicaId(operation.getReplicaId())
					.objectId(operation.getObjectId())
					.entityType(operation.getEntityType())
					.propertyIdentifier(operation.getPropertyIdentifier())
					.oldValue(operation.getOldValueSerialized())
					.newValue(operation.getNewValueSerialized())
					.valueType(operation.getValueType())
					.index(operation.getIndex())
					.vectorClock(vectorClock.copy())
					.build();

			byte[] body = SyncOperationSerializer.serializeToBytes(operationWithClock);

			// Publish with persistent delivery mode
			AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
					.deliveryMode(2) // persistent
					.contentType("application/json")
					.correlationId(operation.getOperationId())
					.build();

			publishChannel.basicPublish(exchangeName, routingKey, props, body);

			logger.fine("Published operation: " + operation);

		} catch (SyncOperationSerializer.SyncSerializationException e) {
			logger.log(Level.SEVERE, "Failed to serialize operation", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to publish operation", e);
			notifyError(e);
		}
	}

	/**
	 * Add a listener for synchronization operations
	 */
	public void addListener(SyncOperationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener
	 */
	public void removeListener(SyncOperationListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Disconnect from RabbitMQ
	 */
	public void disconnect() {
		if (!connected) {
			return;
		}

		closing = true;

		try {
			if (consumeChannel != null && consumeChannel.isOpen()) {
				consumeChannel.close();
			}
			if (publishChannel != null && publishChannel.isOpen()) {
				publishChannel.close();
			}
			if (connection != null && connection.isOpen()) {
				connection.close();
			}
		} catch (IOException | TimeoutException e) {
			logger.log(Level.WARNING, "Error during disconnect", e);
		}

		connected = false;
		closing = false;
		logger.info("Disconnected from RabbitMQ");
		notifyDisconnected("Manual disconnect");
	}

	@Override
	public void close() {
		disconnect();
	}

	private void notifyConnected() {
		for (SyncOperationListener listener : listeners) {
			try {
				listener.onConnected();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error in connection listener", e);
			}
		}
	}

	private void notifyDisconnected(String reason) {
		for (SyncOperationListener listener : listeners) {
			try {
				listener.onDisconnected(reason);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error in disconnection listener", e);
			}
		}
	}

	private void notifyError(Throwable error) {
		for (SyncOperationListener listener : listeners) {
			try {
				listener.onError(error);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error in error listener", e);
			}
		}
	}

	private void notifyStateRequested(String requestingReplicaId) {
		for (SyncOperationListener listener : listeners) {
			try {
				listener.onStateRequested(requestingReplicaId);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error in state request listener", e);
			}
		}
	}

	private void notifyStateReceived(String stateSnapshot, String fromReplicaId) {
		for (SyncOperationListener listener : listeners) {
			try {
				listener.onStateReceived(stateSnapshot, fromReplicaId);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Error in state received listener", e);
			}
		}
	}

	/**
	 * Request the current state from other replicas.
	 * Used when a new client joins and needs to synchronize.
	 */
	@Override
	public void requestState() {
		if (!connected) {
			logger.warning("Cannot request state: not connected to RabbitMQ");
			return;
		}

		try {
			SyncOperation stateRequest = new SyncOperation.Builder(SyncOperation.OperationType.STATE_REQUEST)
					.replicaId(replicaId)
					.objectId("state-request")
					.entityType("StateRequest")
					.vectorClock(vectorClock.copy())
					.build();

			byte[] body = SyncOperationSerializer.serializeToBytes(stateRequest);

			AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
					.deliveryMode(2)
					.contentType("application/json")
					.correlationId(stateRequest.getOperationId())
					.build();

			publishChannel.basicPublish(exchangeName, routingKey, props, body);
			logger.info("Requested state from other replicas");

		} catch (SyncOperationSerializer.SyncSerializationException e) {
			logger.log(Level.SEVERE, "Failed to serialize state request", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to publish state request", e);
			notifyError(e);
		}
	}

	/**
	 * Send the current state as a response to a state request.
	 * 
	 * @param stateSnapshot the serialized state snapshot
	 * @param targetReplicaId the replica that requested the state (optional, null for broadcast)
	 */
	@Override
	public void sendStateResponse(String stateSnapshot, String targetReplicaId) {
		if (!connected) {
			logger.warning("Cannot send state response: not connected to RabbitMQ");
			return;
		}

		try {
			SyncOperation stateResponse = new SyncOperation.Builder(SyncOperation.OperationType.STATE_RESPONSE)
					.replicaId(replicaId)
					.objectId("state-response")
					.entityType("StateResponse")
					.propertyIdentifier(targetReplicaId) // Store target replica ID here
					.newValue(stateSnapshot)
					.vectorClock(vectorClock.copy())
					.build();

			byte[] body = SyncOperationSerializer.serializeToBytes(stateResponse);

			AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
					.deliveryMode(2)
					.contentType("application/json")
					.correlationId(stateResponse.getOperationId())
					.build();

			publishChannel.basicPublish(exchangeName, routingKey, props, body);
			logger.info("Sent state response to replica: " + (targetReplicaId != null ? targetReplicaId : "all"));

		} catch (SyncOperationSerializer.SyncSerializationException e) {
			logger.log(Level.SEVERE, "Failed to serialize state response", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to publish state response", e);
			notifyError(e);
		}
	}

	/**
	 * Builder for creating RabbitMQSyncManager instances
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String host = "localhost";
		private int port = 5672;
		private String username = "guest";
		private String password = "guest";
		private String virtualHost = "/";
		private String exchangeName = "pamela.sync";
		private String routingKey = "operations";
		private boolean useSsl = false;

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder credentials(String username, String password) {
			this.username = username;
			this.password = password;
			return this;
		}

		public Builder virtualHost(String virtualHost) {
			this.virtualHost = virtualHost;
			return this;
		}

		public Builder exchangeName(String exchangeName) {
			this.exchangeName = exchangeName;
			return this;
		}

		public Builder routingKey(String routingKey) {
			this.routingKey = routingKey;
			return this;
		}

		public Builder useSsl(boolean useSsl) {
			this.useSsl = useSsl;
			return this;
		}

		public RabbitMQSyncManager build() {
			return new RabbitMQSyncManager(host, port, username, password, virtualHost, exchangeName, routingKey, useSsl);
		}
	}
}
