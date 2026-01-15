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

import org.openflexo.pamela.factory.EditingContextImpl;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Synchronized editing context that broadcasts PAMELA operations via RabbitMQ.
 * This class extends the standard EditingContext to add real-time collaborative
 * synchronization capabilities.
 * 
 * @author PAMELA Team
 */
public class SyncEditingContext extends EditingContextImpl implements SyncOperationListener {

	private static final Logger logger = Logger.getLogger(SyncEditingContext.class.getName());

	private SyncManager syncManager;
	private final ObjectIdentityManager identityManager;
	private final SyncValueSerializer valueSerializer;
	private PamelaModelFactory modelFactory;

	// Flag to prevent recursive sync when applying remote operations
	private final ThreadLocal<Boolean> applyingRemoteOperation = ThreadLocal.withInitial(() -> false);

	// Stores the replicaId of the remote operation currently being applied
	// Used by ProxyMethodHandler.getCurrentReplicaId() to tag AtomicEdits with the correct replicaId
	private final ThreadLocal<String> currentRemoteReplicaId = new ThreadLocal<>();

	// Buffer for operations during object creation (ensures CREATE is sent before SETs)
	// Key: objectId, Value: list of buffered operations
	private final Map<String, List<SyncOperation>> pendingOperations = new ConcurrentHashMap<>();
	
	// Track objects for which CREATE has been sent (by objectId)
	private final Map<String, Boolean> createdObjects = new ConcurrentHashMap<>();

	// Flag to automatically request state from other replicas on connection
	private boolean autoRequestStateOnConnect = false;

	// Flag to track if state has been received (to avoid multiple requests)
	private volatile boolean stateReceived = false;

	/**
	 * Create a synchronized editing context without a sync manager.
	 * Call setSyncManager() to set one later.
	 */
	public SyncEditingContext() {
		super();
		this.syncManager = null;
		this.identityManager = new ObjectIdentityManager();
		this.valueSerializer = new SyncValueSerializer();
	}

	/**
	 * Create a synchronized editing context with a PamelaModelFactory.
	 * Call setSyncManager() to set a sync manager later.
	 */
	public SyncEditingContext(PamelaModelFactory modelFactory) {
		super();
		this.modelFactory = modelFactory;
		this.syncManager = null;
		this.identityManager = new ObjectIdentityManager();
		this.valueSerializer = new SyncValueSerializer();
	}

	/**
	 * Create a synchronized editing context with custom sync manager
	 */
	public SyncEditingContext(SyncManager syncManager) {
		super();
		this.syncManager = syncManager;
		this.identityManager = new ObjectIdentityManager();
		this.valueSerializer = new SyncValueSerializer();
		if (this.syncManager != null) {
			this.syncManager.addListener(this);
		}
	}

	/**
	 * Set the model factory for this context
	 */
	public void setModelFactory(PamelaModelFactory modelFactory) {
		this.modelFactory = modelFactory;
	}

	/**
	 * Get the sync manager
	 */
	public SyncManager getSyncManager() {
		return syncManager;
	}

	/**
	 * Set the sync manager
	 */
	public void setSyncManager(SyncManager syncManager) {
		this.syncManager = syncManager;
		if (this.syncManager != null) {
			this.syncManager.addListener(this);
		}
	}

	/**
	 * Get the identity manager
	 */
	public ObjectIdentityManager getIdentityManager() {
		return identityManager;
	}

	/**
	 * Get the replica ID
	 */
	public String getReplicaId() {
		return syncManager != null ? syncManager.getReplicaId() : null;
	}

	/**
	 * Check if currently applying a remote operation
	 */
	public boolean isApplyingRemoteOperation() {
		return applyingRemoteOperation.get();
	}

	/**
	 * Get the replicaId of the operation currently being processed.
	 * If applying a remote operation, returns the remote replica's ID.
	 * Otherwise, returns the local replica's ID.
	 * This is used by ProxyMethodHandler to tag AtomicEdits with the correct replicaId.
	 */
	public String getCurrentOperationReplicaId() {
		String remoteId = currentRemoteReplicaId.get();
		if (remoteId != null) {
			return remoteId;
		}
		return getReplicaId();
	}

	/**
	 * Get the interface name for a PAMELA object.
	 * Returns the implemented interface name (e.g., "org.example.Book") instead of
	 * the proxy class name (e.g., "Book$BookImpl_$$_jvst806_1").
	 */
	private <I> String getEntityTypeName(I object) {
		if (modelFactory != null && modelFactory.isProxyObject(object)) {
			ProxyMethodHandler<?> handler = modelFactory.getHandler(object);
			if (handler != null) {
				return handler.getModelEntity().getImplementedInterface().getName();
			}
		}
		return object.getClass().getName();
	}

	/**
	 * Broadcast a SET operation
	 */
	public <I> void broadcastSet(I object, ModelProperty<? super I> property, Object oldValue, Object newValue) {
		if (syncManager == null || isApplyingRemoteOperation() || !syncManager.isConnected()) {
			return;
		}

		try {
			String objectId = identityManager.getOrCreateObjectId(object);
			String entityType = getEntityTypeName(object);

			SyncOperation operation = new SyncOperation.Builder(SyncOperation.OperationType.SET)
					.replicaId(syncManager.getReplicaId())
					.objectId(objectId)
					.entityType(entityType)
					.propertyIdentifier(property.getPropertyIdentifier())
					.oldValue(valueSerializer.serialize(oldValue))
					.newValue(valueSerializer.serialize(newValue))
					.valueType(property.getType().getName())
					.build();

			// Check if CREATE has been sent for this object - if not, buffer the operation
			if (!createdObjects.containsKey(objectId)) {
				// Buffer the operation to be sent after CREATE
				pendingOperations.computeIfAbsent(objectId, k -> new ArrayList<>()).add(operation);
				logger.fine("Buffered SET operation for object not yet created: " + objectId);
			} else {
				syncManager.publishOperation(operation);
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to broadcast SET operation", e);
		}
	}

	/**
	 * Broadcast an ADD operation
	 */
	public <I> void broadcastAdd(I object, ModelProperty<? super I> property, Object addedValue, int index) {
		if (syncManager == null || isApplyingRemoteOperation() || !syncManager.isConnected()) {
			return;
		}

		try {
			String objectId = identityManager.getOrCreateObjectId(object);
			String entityType = getEntityTypeName(object);

			// For PAMELA objects, use reference serialization
			String serializedValue;
			if (addedValue != null && modelFactory != null && modelFactory.isProxyObject(addedValue)) {
				// Ensure added object is registered and use reference
				identityManager.getOrCreateObjectId(addedValue);
				serializedValue = valueSerializer.serializeReference(addedValue, identityManager);
			} else {
				serializedValue = valueSerializer.serialize(addedValue);
			}

			SyncOperation operation = new SyncOperation.Builder(SyncOperation.OperationType.ADD)
					.replicaId(syncManager.getReplicaId())
					.objectId(objectId)
					.entityType(entityType)
					.propertyIdentifier(property.getPropertyIdentifier())
					.newValue(serializedValue)
					.valueType(property.getType().getName())
					.index(index)
					.build();

			syncManager.publishOperation(operation);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to broadcast ADD operation", e);
		}
	}

	/**
	 * Broadcast a REMOVE operation
	 */
	public <I> void broadcastRemove(I object, ModelProperty<? super I> property, Object removedValue) {
		if (syncManager == null || isApplyingRemoteOperation() || !syncManager.isConnected()) {
			return;
		}

		try {
			String objectId = identityManager.getOrCreateObjectId(object);
			String entityType = getEntityTypeName(object);

			// For PAMELA objects, use reference serialization
			String serializedValue;
			if (removedValue != null && modelFactory != null && modelFactory.isProxyObject(removedValue)) {
				serializedValue = valueSerializer.serializeReference(removedValue, identityManager);
			} else {
				serializedValue = valueSerializer.serialize(removedValue);
			}

			SyncOperation operation = new SyncOperation.Builder(SyncOperation.OperationType.REMOVE)
					.replicaId(syncManager.getReplicaId())
					.objectId(objectId)
					.entityType(entityType)
					.propertyIdentifier(property.getPropertyIdentifier())
					.oldValue(serializedValue)
					.valueType(property.getType().getName())
					.build();

			syncManager.publishOperation(operation);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to broadcast REMOVE operation", e);
		}
	}

	/**
	 * Broadcast a CREATE operation
	 */
	public <I> void broadcastCreate(I object, String entityType) {
		if (syncManager == null || isApplyingRemoteOperation() || !syncManager.isConnected()) {
			return;
		}

		try {
			String objectId = identityManager.getOrCreateObjectId(object);

			SyncOperation operation = new SyncOperation.Builder(SyncOperation.OperationType.CREATE)
					.replicaId(syncManager.getReplicaId())
					.objectId(objectId)
					.entityType(entityType)
					.build();

			// Send CREATE first
			syncManager.publishOperation(operation);
			
			// Mark object as created
			createdObjects.put(objectId, Boolean.TRUE);
			
			// Then send any buffered operations for this object
			List<SyncOperation> buffered = pendingOperations.remove(objectId);
			if (buffered != null) {
				for (SyncOperation bufferedOp : buffered) {
					syncManager.publishOperation(bufferedOp);
				}
				logger.fine("Sent " + buffered.size() + " buffered operations for: " + objectId);
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to broadcast CREATE operation", e);
		}
	}

	/**
	 * Broadcast a DELETE operation
	 */
	public <I> void broadcastDelete(I object) {
		if (syncManager == null || isApplyingRemoteOperation() || !syncManager.isConnected()) {
			return;
		}

		try {
			String objectId = identityManager.getObjectId(object);
			if (objectId == null) {
				logger.warning("Cannot broadcast delete for unregistered object");
				return;
			}

			String entityType = getEntityTypeName(object);

			SyncOperation operation = new SyncOperation.Builder(SyncOperation.OperationType.DELETE)
					.replicaId(syncManager.getReplicaId())
					.objectId(objectId)
					.entityType(entityType)
					.build();

			syncManager.publishOperation(operation);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to broadcast DELETE operation", e);
		}
	}

	// SyncOperationListener implementation

	@Override
	public void onOperationReceived(SyncOperation operation) {
		logger.info(">>> Received operation: " + operation.getOperationType() 
				+ " objectId=" + operation.getObjectId() 
				+ " property=" + operation.getPropertyIdentifier());

		if (modelFactory == null) {
			logger.warning("ModelFactory not set, cannot apply remote operation");
			return;
		}

		applyingRemoteOperation.set(true);
		currentRemoteReplicaId.set(operation.getReplicaId());
		try {
			switch (operation.getOperationType()) {
				case CREATE:
					applyRemoteCreate(operation);
					break;
				case DELETE:
					applyRemoteDelete(operation);
					break;
				case SET: case ADD: case REMOVE:
					applyRemoteModification(operation);
					break;
				default:
					logger.warning("Unknown operation type: " + operation.getOperationType());
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to apply remote operation: " + operation, e);
		} finally {
			applyingRemoteOperation.set(false);
			currentRemoteReplicaId.remove();
		}
	}

	@Override
	public void onConnected() {
		logger.info("Sync connected as replica: " + syncManager.getReplicaId());
		
		// Automatically request state from other replicas if enabled
		if (autoRequestStateOnConnect && !stateReceived) {
			logger.info("Automatically requesting state from other replicas...");
			requestStateSync();
		}
	}

	@Override
	public void onDisconnected(String reason) {
		logger.info("Sync disconnected: " + reason);
	}

	@Override
	public void onError(Throwable error) {
		logger.log(Level.SEVERE, "Sync error", error);
	}

	@Override
	public void onStateRequested(String requestingReplicaId) {
		logger.info("State requested by replica: " + requestingReplicaId);
		if (syncManager == null) {
			return;
		}
		
		try {
			String stateSnapshot = serializeCurrentState();
			syncManager.sendStateResponse(stateSnapshot, requestingReplicaId);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to send state response", e);
		}
	}

	@Override
	public void onStateReceived(String stateSnapshot, String fromReplicaId) {
		logger.info("State received from replica: " + fromReplicaId);

		// Mark state as received to prevent duplicate requests
		stateReceived = true;

		// Set the remote replicaId so AtomicEdits are tagged correctly
		currentRemoteReplicaId.set(fromReplicaId);
		try {
			restoreFromSnapshot(stateSnapshot);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to restore from snapshot", e);
		} finally {
			currentRemoteReplicaId.remove();
		}
	}

	/**
	 * Enable or disable automatic state request on connection.
	 * When enabled, the context will automatically request state from other replicas
	 * upon connecting to the sync manager.
	 * 
	 * @param autoRequest true to enable automatic state request
	 */
	public void setAutoRequestStateOnConnect(boolean autoRequest) {
		this.autoRequestStateOnConnect = autoRequest;
	}

	/**
	 * Check if automatic state request on connect is enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isAutoRequestStateOnConnect() {
		return autoRequestStateOnConnect;
	}

	/**
	 * Check if state has been received from another replica.
	 * 
	 * @return true if state was received
	 */
	public boolean isStateReceived() {
		return stateReceived;
	}

	/**
	 * Creates and configures an UndoManager for this sync editing context.
	 * The UndoManager is configured with the local replica ID to filter out remote edits.
	 */
	@Override
	public org.openflexo.pamela.undo.UndoManager createUndoManager() {
		org.openflexo.pamela.undo.UndoManager undoManager = super.createUndoManager();

		// Configure the UndoManager with the local replica ID
		// This ensures it only tracks edits from the local replica
		if (syncManager != null) {
			undoManager.setLocalReplicaId(syncManager.getReplicaId());
		}

		return undoManager;
	}

	/**
	 * Request state from other replicas.
	 * Call this when a new client joins to get the current state.
	 */
	public void requestStateSync() {
		if (syncManager != null && syncManager.isConnected()) {
			syncManager.requestState();
		}
	}

	/**
	 * Serialize the current state of all tracked objects into a snapshot.
	 * 
	 * @return JSON string containing all objects and their properties
	 */
	public String serializeCurrentState() {
		StringBuilder json = new StringBuilder();
		json.append("{\"objects\":[");
		
		Map<String, Object> allObjects = identityManager.getAllObjects();
		boolean first = true;
		
		for (Map.Entry<String, Object> entry : allObjects.entrySet()) {
			String objectId = entry.getKey();
			Object object = entry.getValue();
			
			if (!first) {
				json.append(",");
			}
			first = false;
			
			json.append("{");
			json.append("\"id\":\"").append(escapeJson(objectId)).append("\",");
			json.append("\"type\":\"").append(escapeJson(getEntityTypeName(object))).append("\",");
			json.append("\"properties\":{");
			
			try {
				ProxyMethodHandler<?> handler = modelFactory.getHandler(object);
				if (handler != null) {
					boolean firstProp = true;
					java.util.Iterator<? extends ModelProperty<?>> propIterator = handler.getModelEntity().getProperties();
					while (propIterator.hasNext()) {
						ModelProperty<?> property = propIterator.next();
						if (property.getGetter() != null) {
							Object value = handler.invokeGetter(property.getPropertyIdentifier());
							if (value != null) {
								if (!firstProp) {
									json.append(",");
								}
								firstProp = false;
								
								String serializedValue;
								if (modelFactory.isProxyObject(value)) {
									serializedValue = valueSerializer.serializeReference(value, identityManager);
								} else if (value instanceof List) {
									serializedValue = serializeList((List<?>) value);
								} else {
									serializedValue = valueSerializer.serialize(value);
								}
								
								json.append("\"").append(escapeJson(property.getPropertyIdentifier())).append("\":");
								json.append("\"").append(escapeJson(serializedValue)).append("\"");
							}
						}
					}
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to serialize object: " + objectId, e);
			}
			
			json.append("}}");
		}
		
		json.append("]}");
		return json.toString();
	}

	/**
	 * Restore the local state from a snapshot received from another replica.
	 * 
	 * @param stateSnapshot JSON string containing the state snapshot
	 */
	public void restoreFromSnapshot(String stateSnapshot) {
		if (stateSnapshot == null || stateSnapshot.isEmpty()) {
			logger.warning("Empty state snapshot received");
			return;
		}
		
		applyingRemoteOperation.set(true);
		try {
			// Simple JSON parsing for the snapshot format
			// Format: {"objects":[{"id":"...","type":"...","properties":{...}}, ...]}
			
			int objectsStart = stateSnapshot.indexOf("[");
			int objectsEnd = stateSnapshot.lastIndexOf("]");
			if (objectsStart < 0 || objectsEnd < 0) {
				logger.warning("Invalid snapshot format");
				return;
			}
			
			String objectsJson = stateSnapshot.substring(objectsStart + 1, objectsEnd);
			if (objectsJson.trim().isEmpty()) {
				logger.info("Empty state snapshot - no objects to restore");
				return;
			}
			
			// Parse each object
			List<ObjectSnapshot> snapshots = parseObjectSnapshots(objectsJson);
			
			// First pass: create all objects
			for (ObjectSnapshot snapshot : snapshots) {
				if (!identityManager.hasObject(snapshot.id)) {
					try {
						Class<?> entityClass = Class.forName(snapshot.type);
						Object newObject = modelFactory._newInstance(entityClass, false);
						
						ProxyMethodHandler<?> handler = modelFactory.getHandler(newObject);
						if (handler != null) {
							handler.setDeserializing(true);
						}
						
						identityManager.registerObject(newObject, snapshot.id);
						createdObjects.put(snapshot.id, Boolean.TRUE);
						logger.fine("Created object from snapshot: " + snapshot.id);
					} catch (Exception e) {
						logger.log(Level.WARNING, "Failed to create object from snapshot: " + snapshot.id, e);
					}
				}
			}
			
			// Second pass: set properties (after all objects exist for reference resolution)
			for (ObjectSnapshot snapshot : snapshots) {
				Object object = identityManager.getObject(snapshot.id);
				if (object == null) {
					continue;
				}
				
				try {
					ProxyMethodHandler<?> handler = modelFactory.getHandler(object);
					if (handler != null) {
						for (Map.Entry<String, String> prop : snapshot.properties.entrySet()) {
							try {
								ModelProperty<?> modelProperty = handler.getModelEntity().getModelProperty(prop.getKey());
								if (modelProperty != null) {
									String serializedValue = prop.getValue();
									
									// Handle list properties specially
									if (serializedValue.startsWith("[") && serializedValue.endsWith("]")) {
										// It's a list - use adder for each element
										restoreListProperty(handler, modelProperty, serializedValue);
									} else {
										Object value = valueSerializer.deserialize(serializedValue, modelProperty.getType(), this);
										handler.invokeSetter(prop.getKey(), value);
									}
								}
							} catch (Exception e) {
								logger.log(Level.FINE, "Failed to set property: " + prop.getKey(), e);
							}
						}
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Failed to restore object properties: " + snapshot.id, e);
				}
			}
			
			logger.info("Restored " + snapshots.size() + " objects from snapshot");
			
		} finally {
			applyingRemoteOperation.set(false);
		}
	}

	/**
	 * Restore a list property from serialized format using adder method.
	 */
	private void restoreListProperty(ProxyMethodHandler<?> handler, ModelProperty<?> property, String serializedList) {
		// Remove brackets: [item1,item2,item3] -> item1,item2,item3
		String content = serializedList.substring(1, serializedList.length() - 1);
		if (content.trim().isEmpty()) {
			return; // Empty list
		}
		
		// Split by comma (careful with nested structures)
		List<String> items = splitListItems(content);
		
		for (String item : items) {
			item = item.trim();
			if (item.isEmpty()) {
				continue;
			}
			
			try {
				// Deserialize the item
				Object value = valueSerializer.deserialize(item, property.getType(), this);
				if (value != null) {
					// Use adder to add to the list
					handler.invokeAdder(property.getPropertyIdentifier(), value);
					logger.fine("Added item to list property " + property.getPropertyIdentifier() + ": " + value);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Failed to add item to list: " + item, e);
			}
		}
	}

	/**
	 * Split list items by comma, handling nested structures.
	 */
	private List<String> splitListItems(String content) {
		List<String> items = new ArrayList<>();
		int depth = 0;
		StringBuilder current = new StringBuilder();
		
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '[' || c == '{') {
				depth++;
				current.append(c);
			} else if (c == ']' || c == '}') {
				depth--;
				current.append(c);
			} else if (c == ',' && depth == 0) {
				items.add(current.toString());
				current = new StringBuilder();
			} else {
				current.append(c);
			}
		}
		
		if (current.length() > 0) {
			items.add(current.toString());
		}
		
		return items;
	}

	private String serializeList(List<?> list) {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for (Object item : list) {
			if (!first) {
				sb.append(",");
			}
			first = false;
			
			if (item != null && modelFactory != null && modelFactory.isProxyObject(item)) {
				sb.append(valueSerializer.serializeReference(item, identityManager));
			} else {
				sb.append(valueSerializer.serialize(item));
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private String escapeJson(String value) {
		if (value == null) return "";
		return value.replace("\\", "\\\\")
					.replace("\"", "\\\"")
					.replace("\n", "\\n")
					.replace("\r", "\\r")
					.replace("\t", "\\t");
	}

	private static class ObjectSnapshot {
		String id;
		String type;
		Map<String, String> properties = new java.util.HashMap<>();
	}

	private List<ObjectSnapshot> parseObjectSnapshots(String objectsJson) {
		List<ObjectSnapshot> snapshots = new ArrayList<>();
		
		// Simple parsing - find each object block
		int depth = 0;
		int objectStart = -1;
		
		for (int i = 0; i < objectsJson.length(); i++) {
			char c = objectsJson.charAt(i);
			if (c == '{') {
				if (depth == 0) {
					objectStart = i;
				}
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0 && objectStart >= 0) {
					String objectJson = objectsJson.substring(objectStart, i + 1);
					ObjectSnapshot snapshot = parseObjectSnapshot(objectJson);
					if (snapshot != null) {
						snapshots.add(snapshot);
					}
					objectStart = -1;
				}
			}
		}
		
		return snapshots;
	}

	private ObjectSnapshot parseObjectSnapshot(String objectJson) {
		ObjectSnapshot snapshot = new ObjectSnapshot();
		
		// Extract id
		snapshot.id = extractJsonValue(objectJson, "id");
		snapshot.type = extractJsonValue(objectJson, "type");
		
		if (snapshot.id == null || snapshot.type == null) {
			return null;
		}
		
		// Extract properties block
		int propsStart = objectJson.indexOf("\"properties\":{");
		if (propsStart >= 0) {
			propsStart += 14; // length of "properties":{ 
			int propsEnd = findMatchingBrace(objectJson, propsStart - 1);
			if (propsEnd > propsStart) {
				String propsJson = objectJson.substring(propsStart, propsEnd);
				parseProperties(propsJson, snapshot.properties);
			}
		}
		
		return snapshot;
	}

	private String extractJsonValue(String json, String key) {
		String pattern = "\"" + key + "\":\"";
		int start = json.indexOf(pattern);
		if (start < 0) return null;
		start += pattern.length();
		
		int end = start;
		while (end < json.length()) {
			char c = json.charAt(end);
			if (c == '"' && json.charAt(end - 1) != '\\') {
				break;
			}
			end++;
		}
		
		if (end > start) {
			return unescapeJson(json.substring(start, end));
		}
		return null;
	}

	private int findMatchingBrace(String json, int openPos) {
		int depth = 0;
		for (int i = openPos; i < json.length(); i++) {
			char c = json.charAt(i);
			if (c == '{') depth++;
			else if (c == '}') {
				depth--;
				if (depth == 0) return i;
			}
		}
		return -1;
	}

	private void parseProperties(String propsJson, Map<String, String> properties) {
		// Simple key-value parsing for "key":"value" pairs
		int i = 0;
		while (i < propsJson.length()) {
			// Find key start
			int keyStart = propsJson.indexOf('"', i);
			if (keyStart < 0) break;
			keyStart++;
			
			int keyEnd = propsJson.indexOf('"', keyStart);
			if (keyEnd < 0) break;
			
			String key = propsJson.substring(keyStart, keyEnd);
			
			// Find value after ":"
			int colonPos = propsJson.indexOf(':', keyEnd);
			if (colonPos < 0) break;
			
			int valueStart = propsJson.indexOf('"', colonPos);
			if (valueStart < 0) break;
			valueStart++;
			
			int valueEnd = valueStart;
			while (valueEnd < propsJson.length()) {
				char c = propsJson.charAt(valueEnd);
				if (c == '"' && propsJson.charAt(valueEnd - 1) != '\\') {
					break;
				}
				valueEnd++;
			}
			
			if (valueEnd > valueStart) {
				properties.put(key, unescapeJson(propsJson.substring(valueStart, valueEnd)));
			}
			
			i = valueEnd + 1;
		}
	}

	private String unescapeJson(String value) {
		return value.replace("\\\"", "\"")
					.replace("\\\\", "\\")
					.replace("\\n", "\n")
					.replace("\\r", "\r")
					.replace("\\t", "\t");
	}

	// Private methods for applying remote operations

	private void applyRemoteModification(SyncOperation operation){
		Object target = identityManager.getObject(operation.getObjectId());
		if (target == null) 
			// Object doesn't exist yet - try to create it first (might happen because of reordering operations)			
			target = ensureRemoteObjectExists(operation.getObjectId(), operation.getEntityType());			
		
		try {
			ProxyMethodHandler<?> handler = modelFactory.getHandler(target);
			if (handler != null) {
				ModelProperty<?> property = handler.getModelEntity().getModelProperty(operation.getPropertyIdentifier());
				if (property != null) {
					Object newValue = valueSerializer.deserialize(
							operation.getNewValueSerialized(),
							property.getType(),
							this
					);
					switch(operation.getOperationType()){
						case SET:
						handler.invokeSetter(operation.getPropertyIdentifier(), newValue); 
						break; 
						case ADD: 	
						handler.invokeAdder(operation.getPropertyIdentifier(), newValue);
						break; 
						case REMOVE: 
						handler.invokeRemover(operation.getPropertyIdentifier(), newValue); 
						break; 
						default: 
						break; 
					}					
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to apply" + operation.getOperationType(), e);
		}
	}

	
	private void applyRemoteCreate(SyncOperation operation) {
		// Check if object already exists
		if (identityManager.hasObject(operation.getObjectId())) {
			logger.fine("Object already exists: " + operation.getObjectId());
			return;
		}

		try {
			// Load the entity class
			Class<?> entityClass = Class.forName(operation.getEntityType());

			// Create a new instance using _newInstance (like deserialization does)
			// This creates the object without requiring the initializer
			Object newObject = modelFactory._newInstance(entityClass, false);
			
			// Mark the object as deserializing so it can receive property updates
			// without failing the "uninitialized" check
			ProxyMethodHandler<?> handler = modelFactory.getHandler(newObject);
			if (handler != null) {
				handler.setDeserializing(true);
			}

			// Register with the specified ID
			identityManager.registerObject(newObject, operation.getObjectId());
			
			// Mark as known so SET operations work properly
			createdObjects.put(operation.getObjectId(), Boolean.TRUE);

			logger.fine("Created remote object: " + operation.getObjectId());

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to apply remote CREATE", e);
		}
	}

	private void applyRemoteDelete(SyncOperation operation) {
		Object target = identityManager.getObject(operation.getObjectId());
		if (target == null) {
			logger.fine("Object already deleted or not found: " + operation.getObjectId());
			return;
		}

		try {
			ProxyMethodHandler<?> handler = modelFactory.getHandler(target);
			if (handler != null) {
				handler.invokeDeleter(target);
			}

			identityManager.unregisterById(operation.getObjectId());

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to apply remote DELETE", e);
		}
	
	}

	/**
	 * Ensure a remote object exists, creating it if necessary.
	 * This handles the case where operations arrive out of order.
	 * 
	 * @param objectId the object ID
	 * @param entityType the entity class name
	 * @return the object, or null if creation failed
	 */
	private Object ensureRemoteObjectExists(String objectId, String entityType) {					
		if (entityType == null) {
			logger.warning("Cannot create object without entityType for ID: " + objectId);
			return null;
		}

		try {
			Class<?> entityClass = Class.forName(entityType);
			
			// Create using _newInstance (bypasses initializer requirement)
			Object newObject = modelFactory._newInstance(entityClass, false);
			
			// Mark as deserializing to allow setters without initialization
			ProxyMethodHandler<?> handler = modelFactory.getHandler(newObject);
			if (handler != null) {
				handler.setDeserializing(true);
			}
			
			// Register with the specified ID
			identityManager.registerObject(newObject, objectId);
			
			// Mark as known so SET operations work properly
			createdObjects.put(objectId, Boolean.TRUE);
			
			logger.fine("Auto-created remote object: " + objectId);
			return newObject;
			
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to auto-create remote object: " + objectId, e);
			return null;
		}
	}
}
