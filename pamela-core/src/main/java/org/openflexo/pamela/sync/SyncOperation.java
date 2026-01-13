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

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a synchronization operation that can be shared between PAMELA instances
 * via RabbitMQ for collaborative real-time editing.
 * 
 * @author PAMELA Team
 */
public class SyncOperation implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Types of operations that can be synchronized
	 */
	public enum OperationType {
		CREATE,
		DELETE,
		SET,
		ADD,
		REMOVE,
		REINDEX,
		STATE_REQUEST,
		STATE_RESPONSE
	}

	// Operation identification
	private final String operationId;
	private final OperationType operationType;
	private final long timestamp;
	private final String replicaId;

	// Target object information
	private final String objectId;
	private final String entityType;

	// Property information (for SET, ADD, REMOVE, REINDEX)
	private final String propertyIdentifier;

	// Value information
	private final String oldValueSerialized;
	private final String newValueSerialized;
	private final String valueType;

	// For list operations
	private final int index;

	// Vector clock for causality tracking
	private final VectorClock vectorClock;

	private SyncOperation(Builder builder) {
		this.operationId = builder.operationId != null ? builder.operationId : UUID.randomUUID().toString();
		this.operationType = builder.operationType;
		this.timestamp = builder.timestamp > 0 ? builder.timestamp : System.currentTimeMillis();
		this.replicaId = builder.replicaId;
		this.objectId = builder.objectId;
		this.entityType = builder.entityType;
		this.propertyIdentifier = builder.propertyIdentifier;
		this.oldValueSerialized = builder.oldValueSerialized;
		this.newValueSerialized = builder.newValueSerialized;
		this.valueType = builder.valueType;
		this.index = builder.index;
		this.vectorClock = builder.vectorClock;
	}

	// Getters
	public String getOperationId() {
		return operationId;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getReplicaId() {
		return replicaId;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getPropertyIdentifier() {
		return propertyIdentifier;
	}

	public String getOldValueSerialized() {
		return oldValueSerialized;
	}

	public String getNewValueSerialized() {
		return newValueSerialized;
	}

	public String getValueType() {
		return valueType;
	}

	public int getIndex() {
		return index;
	}

	public VectorClock getVectorClock() {
		return vectorClock;
	}

	@Override
	public String toString() {
		return "SyncOperation{" +
				"type=" + operationType +
				", objectId='" + objectId + '\'' +
				", property='" + propertyIdentifier + '\'' +
				", replica='" + replicaId + '\'' +
				'}';
	}

	/**
	 * Builder for creating SyncOperation instances
	 */
	public static class Builder {
		private String operationId;
		private OperationType operationType;
		private long timestamp;
		private String replicaId;
		private String objectId;
		private String entityType;
		private String propertyIdentifier;
		private String oldValueSerialized;
		private String newValueSerialized;
		private String valueType;
		private int index = -1;
		private VectorClock vectorClock;

		public Builder(OperationType operationType) {
			this.operationType = operationType;
		}

		public Builder operationId(String operationId) {
			this.operationId = operationId;
			return this;
		}

		public Builder timestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder replicaId(String replicaId) {
			this.replicaId = replicaId;
			return this;
		}

		public Builder objectId(String objectId) {
			this.objectId = objectId;
			return this;
		}

		public Builder entityType(String entityType) {
			this.entityType = entityType;
			return this;
		}

		public Builder propertyIdentifier(String propertyIdentifier) {
			this.propertyIdentifier = propertyIdentifier;
			return this;
		}

		public Builder oldValue(String oldValueSerialized) {
			this.oldValueSerialized = oldValueSerialized;
			return this;
		}

		public Builder newValue(String newValueSerialized) {
			this.newValueSerialized = newValueSerialized;
			return this;
		}

		public Builder valueType(String valueType) {
			this.valueType = valueType;
			return this;
		}

		public Builder index(int index) {
			this.index = index;
			return this;
		}

		public Builder vectorClock(VectorClock vectorClock) {
			this.vectorClock = vectorClock;
			return this;
		}

		public SyncOperation build() {
			if (operationType == null) {
				throw new IllegalStateException("Operation type is required");
			}
			if (replicaId == null) {
				throw new IllegalStateException("Replica ID is required");
			}
			if (objectId == null) {
				throw new IllegalStateException("Object ID is required");
			}
			return new SyncOperation(this);
		}
	}
}
