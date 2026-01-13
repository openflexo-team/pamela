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

/**
 * Listener interface for receiving synchronization operations from remote replicas.
 * Implementations handle the application of remote operations to the local model.
 * 
 * @author PAMELA Team
 */
public interface SyncOperationListener {

	/**
	 * Called when a synchronization operation is received from a remote replica
	 * 
	 * @param operation the received operation
	 */
	void onOperationReceived(SyncOperation operation);

	/**
	 * Called when the synchronization connection is established
	 */
	default void onConnected() {
	}

	/**
	 * Called when the synchronization connection is lost
	 * 
	 * @param reason the reason for disconnection
	 */
	default void onDisconnected(String reason) {
	}

	/**
	 * Called when a synchronization error occurs
	 * 
	 * @param error the error that occurred
	 */
	default void onError(Throwable error) {
	}

	/**
	 * Called when a state request is received from a new replica.
	 * The listener should respond by sending the current state.
	 * 
	 * @param requestingReplicaId the ID of the replica requesting state
	 */
	default void onStateRequested(String requestingReplicaId) {
	}

	/**
	 * Called when a state snapshot is received from another replica.
	 * The listener should restore the local state from the snapshot.
	 * 
	 * @param stateSnapshot the serialized state snapshot
	 * @param fromReplicaId the ID of the replica that sent the state
	 */
	default void onStateReceived(String stateSnapshot, String fromReplicaId) {
	}
}
