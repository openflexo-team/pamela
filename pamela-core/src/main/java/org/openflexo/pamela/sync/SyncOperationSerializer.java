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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serializer for SyncOperation objects.
 * Handles conversion to/from JSON for transmission over RabbitMQ.
 * 
 * @author PAMELA Team
 */
public class SyncOperationSerializer {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Serialize a SyncOperation to JSON string
	 * 
	 * @param operation the operation to serialize
	 * @return JSON string representation
	 * @throws SyncSerializationException if serialization fails
	 */
	public static String serialize(SyncOperation operation) throws SyncSerializationException {
		try {
			return objectMapper.writeValueAsString(new SyncOperationDTO(operation));
		} catch (JsonProcessingException e) {
			throw new SyncSerializationException("Failed to serialize SyncOperation", e);
		}
	}

	/**
	 * Deserialize a JSON string to SyncOperation
	 * 
	 * @param json the JSON string
	 * @return the deserialized SyncOperation
	 * @throws SyncSerializationException if deserialization fails
	 */
	public static SyncOperation deserialize(String json) throws SyncSerializationException {
		try {
			SyncOperationDTO dto = objectMapper.readValue(json, SyncOperationDTO.class);
			return dto.toSyncOperation();
		} catch (JsonProcessingException e) {
			throw new SyncSerializationException("Failed to deserialize SyncOperation", e);
		}
	}

	/**
	 * Serialize a SyncOperation to byte array
	 * 
	 * @param operation the operation to serialize
	 * @return byte array representation
	 * @throws SyncSerializationException if serialization fails
	 */
	public static byte[] serializeToBytes(SyncOperation operation) throws SyncSerializationException {
		try {
			return objectMapper.writeValueAsBytes(new SyncOperationDTO(operation));
		} catch (JsonProcessingException e) {
			throw new SyncSerializationException("Failed to serialize SyncOperation to bytes", e);
		}
	}

	/**
	 * Deserialize a byte array to SyncOperation
	 * 
	 * @param bytes the byte array
	 * @return the deserialized SyncOperation
	 * @throws SyncSerializationException if deserialization fails
	 */
	public static SyncOperation deserializeFromBytes(byte[] bytes) throws SyncSerializationException {
		try {
			SyncOperationDTO dto = objectMapper.readValue(bytes, SyncOperationDTO.class);
			return dto.toSyncOperation();
		} catch (Exception e) {
			throw new SyncSerializationException("Failed to deserialize SyncOperation from bytes", e);
		}
	}

	/**
	 * DTO class for JSON serialization
	 */
	public static class SyncOperationDTO {
		public String operationId;
		public String operationType;
		public long timestamp;
		public String replicaId;
		public String objectId;
		public String entityType;
		public String propertyIdentifier;
		public String oldValueSerialized;
		public String newValueSerialized;
		public String valueType;
		public int index;
		public VectorClockDTO vectorClock;

		public SyncOperationDTO() {
		}

		public SyncOperationDTO(SyncOperation op) {
			this.operationId = op.getOperationId();
			this.operationType = op.getOperationType().name();
			this.timestamp = op.getTimestamp();
			this.replicaId = op.getReplicaId();
			this.objectId = op.getObjectId();
			this.entityType = op.getEntityType();
			this.propertyIdentifier = op.getPropertyIdentifier();
			this.oldValueSerialized = op.getOldValueSerialized();
			this.newValueSerialized = op.getNewValueSerialized();
			this.valueType = op.getValueType();
			this.index = op.getIndex();
			if (op.getVectorClock() != null) {
				this.vectorClock = new VectorClockDTO(op.getVectorClock());
			}
		}

		public SyncOperation toSyncOperation() {
			VectorClock vc = null;
			if (vectorClock != null) {
				vc = vectorClock.toVectorClock();
			}

			return new SyncOperation.Builder(SyncOperation.OperationType.valueOf(operationType))
					.operationId(operationId)
					.timestamp(timestamp)
					.replicaId(replicaId)
					.objectId(objectId)
					.entityType(entityType)
					.propertyIdentifier(propertyIdentifier)
					.oldValue(oldValueSerialized)
					.newValue(newValueSerialized)
					.valueType(valueType)
					.index(index)
					.vectorClock(vc)
					.build();
		}
	}

	/**
	 * DTO class for VectorClock JSON serialization
	 */
	public static class VectorClockDTO {
		public java.util.Map<String, Long> clock;

		public VectorClockDTO() {
		}

		public VectorClockDTO(VectorClock vc) {
			this.clock = vc.getClockMap();
		}

		public VectorClock toVectorClock() {
			return new VectorClock(clock != null ? clock : new java.util.HashMap<>());
		}
	}

	/**
	 * Exception for serialization errors
	 */
	public static class SyncSerializationException extends Exception {
		public SyncSerializationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
