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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serializer for property values in sync operations.
 * Handles conversion of PAMELA property values to/from string representation
 * for transmission over RabbitMQ.
 * 
 * @author PAMELA Team
 */
public class SyncValueSerializer {

	private static final Logger logger = Logger.getLogger(SyncValueSerializer.class.getName());

	// Special markers for null and object references
	private static final String NULL_MARKER = "__NULL__";
	private static final String OBJECT_REF_PREFIX = "__REF__:";

	/**
	 * Serialize a value to string representation
	 * 
	 * @param value the value to serialize
	 * @return string representation
	 */
	public String serialize(Object value) {
		if (value == null) {
			return NULL_MARKER;
		}

		// Handle primitive types and common types
		if (value instanceof String) {
			return (String) value;
		}

		if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}

		if (value instanceof Enum) {
			return ((Enum<?>) value).name();
		}

		// Handle PAMELA objects by reference
		// This requires the object to be registered in the identity manager
		// For now, we'll use toString() and hope for the best
		// A more robust solution would check if it's a proxy object

		return value.toString();
	}

	/**
	 * Serialize a PAMELA object reference
	 * 
	 * @param object the PAMELA object
	 * @param identityManager the identity manager to get the object ID
	 * @return reference string
	 */
	public String serializeReference(Object object, ObjectIdentityManager identityManager) {
		if (object == null) {
			return NULL_MARKER;
		}

		String objectId = identityManager.getObjectId(object);
		if (objectId != null) {
			return OBJECT_REF_PREFIX + objectId;
		}

		// Not a registered object, serialize as string
		return serialize(object);
	}

	/**
	 * Deserialize a string representation to a value
	 * 
	 * @param serialized the serialized string
	 * @param targetType the expected type
	 * @param syncContext the sync context for resolving object references
	 * @return the deserialized value
	 */
	public Object deserialize(String serialized, Class<?> targetType, SyncEditingContext syncContext) {
		if (serialized == null || NULL_MARKER.equals(serialized)) {
			return null;
		}

		// Handle object references
		if (serialized.startsWith(OBJECT_REF_PREFIX)) {
			String objectId = serialized.substring(OBJECT_REF_PREFIX.length());
			return syncContext.getIdentityManager().getObject(objectId);
		}

		try {
			// Handle primitive types
			if (targetType == String.class) {
				return serialized;
			}

			if (targetType == int.class || targetType == Integer.class) {
				return Integer.parseInt(serialized);
			}

			if (targetType == long.class || targetType == Long.class) {
				return Long.parseLong(serialized);
			}

			if (targetType == double.class || targetType == Double.class) {
				return Double.parseDouble(serialized);
			}

			if (targetType == float.class || targetType == Float.class) {
				return Float.parseFloat(serialized);
			}

			if (targetType == boolean.class || targetType == Boolean.class) {
				return Boolean.parseBoolean(serialized);
			}

			if (targetType == byte.class || targetType == Byte.class) {
				return Byte.parseByte(serialized);
			}

			if (targetType == short.class || targetType == Short.class) {
				return Short.parseShort(serialized);
			}

			if (targetType == char.class || targetType == Character.class) {
				return serialized.isEmpty() ? '\0' : serialized.charAt(0);
			}

			// Handle enums
			if (targetType.isEnum()) {
				@SuppressWarnings({"unchecked", "rawtypes"})
				Object enumValue = Enum.valueOf((Class<Enum>) targetType, serialized);
				return enumValue;
			}

			// For other types, return the string and let PAMELA's converters handle it
			return serialized;

		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to deserialize value: " + serialized + " to type " + targetType, e);
			return serialized;
		}
	}

	/**
	 * Check if a serialized value is an object reference
	 * 
	 * @param serialized the serialized string
	 * @return true if it's an object reference
	 */
	public boolean isObjectReference(String serialized) {
		return serialized != null && serialized.startsWith(OBJECT_REF_PREFIX);
	}

	/**
	 * Extract the object ID from a reference string
	 * 
	 * @param serialized the serialized reference
	 * @return the object ID, or null if not a reference
	 */
	public String extractObjectId(String serialized) {
		if (isObjectReference(serialized)) {
			return serialized.substring(OBJECT_REF_PREFIX.length());
		}
		return null;
	}
}
