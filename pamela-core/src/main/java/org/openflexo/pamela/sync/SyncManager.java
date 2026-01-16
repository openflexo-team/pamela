/**
 * Copyright (c) 2013-2015, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 */

package org.openflexo.pamela.sync;

/**
 * Interface for synchronization managers that handle the transport layer
 * for PAMELA collaborative synchronization.
 * 
 * Implementations can use different messaging systems like RabbitMQ, Redis,
 * WebSockets, or any other pub/sub mechanism.
 * 
 * @author PAMELA Sync
 */
public interface SyncManager {

	/**
	 * Publishes a synchronization operation to all other replicas.
	 * 
	 * @param operation the operation to publish
	 */
	void publishOperation(SyncOperation operation);

	/**
	 * Adds a listener to receive operations from other replicas.
	 * 
	 * @param listener the listener to add
	 */
	void addListener(SyncOperationListener listener);

	/**
	 * Removes a previously registered listener.
	 * 
	 * @param listener the listener to remove
	 */
	void removeListener(SyncOperationListener listener);

	/**
	 * Returns whether this sync manager is currently connected to the messaging system.
	 * 
	 * @return true if connected, false otherwise
	 */
	boolean isConnected();

	/**
	 * Returns the unique identifier of this replica.
	 * 
	 * @return the replica ID
	 */
	String getReplicaId();

	/**
	 * Requests the current state from other replicas.
	 * Used when a new client joins and needs to synchronize.
	 */
	default void requestState() {
		// Default implementation does nothing
	}

	/**
	 * Sends the current state as a response to a state request.
	 * 
	 * @param stateSnapshot the serialized state snapshot
	 * @param targetReplicaId the replica that requested the state (optional, null for broadcast)
	 */
	default void sendStateResponse(String stateSnapshot, String targetReplicaId) {
		// Default implementation does nothing
	}

}
