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

package org.openflexo.pamela.test.sync;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.sync.ObjectIdentityManager;
import org.openflexo.pamela.sync.SyncEditingContext;
import org.openflexo.pamela.sync.SyncOperation;
import org.openflexo.pamela.sync.SyncOperationListener;
import org.openflexo.pamela.sync.SyncOperationSerializer;
import org.openflexo.pamela.sync.VectorClock;

/**
 * Unit tests for the synchronization infrastructure without requiring RabbitMQ.
 * These tests validate the core sync components in isolation.
 * 
 * @author PAMELA Sync Test
 */
public class SyncInfrastructureTest {

	private PamelaModelFactory factory;
	private SyncEditingContext context;
	private List<SyncOperation> capturedOperations;

	@Before
	public void setUp() throws Exception {
		factory = new PamelaModelFactory(CollaborativeDocument.class);
		context = new SyncEditingContext(factory);
		factory.setEditingContext(context);
		capturedOperations = new ArrayList<>();

		// Use a mock sync manager that captures operations locally
		context.setSyncManager(new LocalCaptureSyncManager(capturedOperations));
	}

	// ========== Vector Clock Tests ==========

	@Test
	public void testVectorClockIncrement() {
		VectorClock clock = new VectorClock();
		
		assertEquals(0, clock.get("replica1"));
		
		clock.increment("replica1");
		assertEquals(1, clock.get("replica1"));
		
		clock.increment("replica1");
		assertEquals(2, clock.get("replica1"));
	}

	@Test
	public void testVectorClockMerge() {
		VectorClock clockA = new VectorClock();
		VectorClock clockB = new VectorClock();

		clockA.increment("A"); // A: {A=1}
		clockA.increment("A"); // A: {A=2}

		clockB.increment("B"); // B: {B=1}

		clockA.merge(clockB); // A: {A=2, B=1}

		assertEquals(2, clockA.get("A"));
		assertEquals(1, clockA.get("B"));
	}

	@Test
	public void testVectorClockHappenedBefore() {
		VectorClock clock1 = new VectorClock();
		VectorClock clock2 = new VectorClock();

		clock1.increment("A"); // {A=1}
		clock2.increment("A"); // {A=1}
		clock2.increment("A"); // {A=2}

		assertTrue("clock1 should happen before clock2", clock1.happenedBefore(clock2));
		assertFalse("clock2 should NOT happen before clock1", clock2.happenedBefore(clock1));
	}

	@Test
	public void testVectorClockConcurrent() {
		VectorClock clockA = new VectorClock();
		VectorClock clockB = new VectorClock();

		clockA.increment("A"); // A: {A=1}
		clockB.increment("B"); // B: {B=1}

		assertTrue("Concurrent operations should be detected", clockA.isConcurrent(clockB));
		assertTrue("Concurrent is symmetric", clockB.isConcurrent(clockA));
	}

	// ========== Object Identity Manager Tests ==========

	@Test
	public void testObjectIdentityRegistration() {
		ObjectIdentityManager manager = new ObjectIdentityManager();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		
		String id1 = manager.getOrCreateObjectId(doc);
		assertNotNull("Should generate ID", id1);

		String id2 = manager.getOrCreateObjectId(doc);
		assertEquals("Same object should return same ID", id1, id2);
	}

	@Test
	public void testObjectIdentityLookup() {
		ObjectIdentityManager manager = new ObjectIdentityManager();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		String id = manager.getOrCreateObjectId(doc);

		Object retrieved = manager.getObject(id);
		assertSame("Should retrieve the same object", doc, retrieved);
	}

	@Test
	public void testObjectIdentityExplicitRegistration() {
		ObjectIdentityManager manager = new ObjectIdentityManager();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		String customId = "my-custom-id-12345";

		manager.registerObject(doc, customId);

		assertEquals("Should use custom ID", customId, manager.getOrCreateObjectId(doc));
		assertSame("Should retrieve by custom ID", doc, manager.getObject(customId));
	}

	// ========== SyncOperation Serialization Tests ==========

	@Test
	public void testSyncOperationSerialization() throws Exception {
		VectorClock clock = new VectorClock();
		clock.increment("replica1");

		SyncOperation original = new SyncOperation.Builder(SyncOperation.OperationType.SET)
				.objectId("obj-123")
				.entityType("CollaborativeDocument")
				.propertyIdentifier("title")
				.oldValue("Old Title")
				.newValue("New Title")
				.vectorClock(clock)
				.replicaId("replica1")
				.build();

		SyncOperationSerializer serializer = new SyncOperationSerializer();

		// Serialize to JSON
		String json = serializer.serialize(original);
		assertNotNull("Should serialize to JSON", json);
		assertTrue("JSON should contain operation type", json.contains("SET"));
		assertTrue("JSON should contain property", json.contains("title"));

		// Deserialize back
		SyncOperation deserialized = serializer.deserialize(json);
		assertNotNull("Should deserialize", deserialized);
		assertEquals("Operation type should match", original.getOperationType(), deserialized.getOperationType());
		assertEquals("Object ID should match", original.getObjectId(), deserialized.getObjectId());
		assertEquals("Property should match", original.getPropertyIdentifier(), deserialized.getPropertyIdentifier());
		assertEquals("New value should match", original.getNewValueSerialized(), deserialized.getNewValueSerialized());
		assertEquals("Replica ID should match", original.getReplicaId(), deserialized.getReplicaId());
	}

	@Test
	public void testSyncOperationBytesSerialization() throws Exception {
		SyncOperation original = new SyncOperation.Builder(SyncOperation.OperationType.ADD)
				.objectId("obj-456")
				.entityType("CollaborativeDocument")
				.propertyIdentifier("tags")
				.newValue("new-tag")
				.replicaId("replica2")
				.build();

		SyncOperationSerializer serializer = new SyncOperationSerializer();

		byte[] bytes = serializer.serializeToBytes(original);
		assertNotNull("Should serialize to bytes", bytes);
		assertTrue("Bytes should not be empty", bytes.length > 0);

		SyncOperation deserialized = serializer.deserializeFromBytes(bytes);
		assertNotNull("Should deserialize from bytes", deserialized);
		assertEquals(SyncOperation.OperationType.ADD, deserialized.getOperationType());
		assertEquals("tags", deserialized.getPropertyIdentifier());
	}

	// ========== SyncEditingContext Operation Capture Tests ==========

	@Test
	public void testSetOperationCapture() {
		capturedOperations.clear();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		
		// Modify the document
		doc.setTitle("Test Title");

		// Check that a SET operation was captured
		assertFalse("Should capture SET operation", capturedOperations.isEmpty());
		
		SyncOperation op = findOperationByProperty("title");
		assertNotNull("Should have title operation", op);
		assertEquals(SyncOperation.OperationType.SET, op.getOperationType());
		assertEquals("Test Title", op.getNewValueSerialized());
	}

	@Test
	public void testAddOperationCapture() {
		capturedOperations.clear();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		
		// Add a tag
		doc.addToTags("java");

		// Check that an ADD operation was captured
		SyncOperation op = findOperationByProperty("tags");
		assertNotNull("Should have tags operation", op);
		assertEquals(SyncOperation.OperationType.ADD, op.getOperationType());
		assertEquals("java", op.getNewValueSerialized());
	}

	@Test
	public void testRemoveOperationCapture() {
		capturedOperations.clear();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		doc.addToTags("java");
		
		capturedOperations.clear(); // Clear the ADD operation
		
		// Remove the tag
		doc.removeFromTags("java");

		// Check that a REMOVE operation was captured
		SyncOperation op = findOperationByProperty("tags");
		assertNotNull("Should have tags operation", op);
		assertEquals(SyncOperation.OperationType.REMOVE, op.getOperationType());
	}

	@Test
	public void testMultipleOperationsCapture() {
		capturedOperations.clear();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		
		doc.setTitle("My Document");
		doc.setAuthor("John Doe");
		doc.setContent("Some content here");
		doc.addToTags("tag1");
		doc.addToTags("tag2");

		// Should have captured multiple operations
		assertTrue("Should have multiple operations", capturedOperations.size() >= 5);

		// Verify we have the expected operations
		assertNotNull("Should have title op", findOperationByProperty("title"));
		assertNotNull("Should have author op", findOperationByProperty("author"));
		assertNotNull("Should have content op", findOperationByProperty("content"));
	}

	// ========== Nested Entity Tests ==========

	@Test
	public void testNestedEntityOperations() {
		capturedOperations.clear();

		CollaborativeDocument doc = factory.newInstance(CollaborativeDocument.class);
		DocumentSection section = factory.newInstance(DocumentSection.class);
		
		section.setHeading("Introduction");
		section.setBody("This is the introduction section.");
		
		capturedOperations.clear();
		
		doc.addToSections(section);

		// Should capture the ADD operation for sections
		SyncOperation op = findOperationByProperty("sections");
		assertNotNull("Should have sections ADD operation", op);
		assertEquals(SyncOperation.OperationType.ADD, op.getOperationType());
	}

	// ========== Helper Methods ==========

	private SyncOperation findOperationByProperty(String propertyName) {
		for (SyncOperation op : capturedOperations) {
			if (propertyName.equals(op.getPropertyIdentifier())) {
				return op;
			}
		}
		return null;
	}

	/**
	 * A local mock sync manager that captures operations without network communication.
	 */
	private static class LocalCaptureSyncManager implements org.openflexo.pamela.sync.SyncManager {
		private final List<SyncOperation> capturedOperations;
		private final List<SyncOperationListener> listeners = new ArrayList<>();

		public LocalCaptureSyncManager(List<SyncOperation> capturedOperations) {
			this.capturedOperations = capturedOperations;
		}

		@Override
		public void publishOperation(SyncOperation operation) {
			capturedOperations.add(operation);
			// Notify listeners (simulating message reception)
			for (SyncOperationListener listener : listeners) {
				listener.onOperationReceived(operation);
			}
		}

		@Override
		public void addListener(SyncOperationListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeListener(SyncOperationListener listener) {
			listeners.remove(listener);
		}

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public String getReplicaId() {
			return "test-replica";
		}
	}
}
