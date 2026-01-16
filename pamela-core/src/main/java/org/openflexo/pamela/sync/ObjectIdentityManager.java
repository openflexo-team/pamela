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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages unique identifiers for PAMELA objects across distributed instances.
 * Each object gets a UUID that is stable across all replicas.
 * 
 * @author PAMELA Team
 */
public class ObjectIdentityManager {

	// Maps objects to their unique IDs
	private final Map<Object, String> objectToId = new ConcurrentHashMap<>();

	// Maps IDs back to objects
	private final Map<String, Object> idToObject = new ConcurrentHashMap<>();

	/**
	 * Register an object with a new generated UUID
	 * 
	 * @param object the object to register
	 * @return the generated UUID
	 */
	public String registerObject(Object object) {
		String existingId = objectToId.get(object);
		if (existingId != null) {
			return existingId;
		}

		String newId = UUID.randomUUID().toString();
		objectToId.put(object, newId);
		idToObject.put(newId, object);
		return newId;
	}

	/**
	 * Register an object with a specific ID (used when receiving remote objects)
	 * 
	 * @param object the object to register
	 * @param objectId the specific ID to use
	 */
	public void registerObject(Object object, String objectId) {
		objectToId.put(object, objectId);
		idToObject.put(objectId, object);
	}

	/**
	 * Get the ID for an object
	 * 
	 * @param object the object
	 * @return the object's ID, or null if not registered
	 */
	public String getObjectId(Object object) {
		return objectToId.get(object);
	}

	/**
	 * Get the ID for an object, registering it if necessary
	 * 
	 * @param object the object
	 * @return the object's ID
	 */
	public String getOrCreateObjectId(Object object) {
		String id = objectToId.get(object);
		if (id == null) {
			id = registerObject(object);
		}
		return id;
	}

	/**
	 * Get an object by its ID
	 * 
	 * @param objectId the object ID
	 * @return the object, or null if not found
	 */
	public Object getObject(String objectId) {
		return idToObject.get(objectId);
	}

	/**
	 * Check if an object is registered
	 * 
	 * @param object the object
	 * @return true if registered
	 */
	public boolean isRegistered(Object object) {
		return objectToId.containsKey(object);
	}

	/**
	 * Check if an ID is registered
	 * 
	 * @param objectId the object ID
	 * @return true if registered
	 */
	public boolean hasObject(String objectId) {
		return idToObject.containsKey(objectId);
	}

	/**
	 * Unregister an object
	 * 
	 * @param object the object to unregister
	 */
	public void unregisterObject(Object object) {
		String id = objectToId.remove(object);
		if (id != null) {
			idToObject.remove(id);
		}
	}

	/**
	 * Unregister an object by ID
	 * 
	 * @param objectId the object ID
	 */
	public void unregisterById(String objectId) {
		Object object = idToObject.remove(objectId);
		if (object != null) {
			objectToId.remove(object);
		}
	}

	/**
	 * Clear all registrations
	 */
	public void clear() {
		objectToId.clear();
		idToObject.clear();
	}

	/**
	 * Get the number of registered objects
	 * 
	 * @return the count
	 */
	public int size() {
		return objectToId.size();
	}

	/**
	 * Get all registered objects as a map from ID to object.
	 * Returns a copy of the internal map to prevent modification.
	 * 
	 * @return a map of object ID to object
	 */
	public Map<String, Object> getAllObjects() {
		return new ConcurrentHashMap<>(idToObject);
	}
}
