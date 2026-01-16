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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.sync.ObjectIdentityManager;
import org.openflexo.pamela.sync.RabbitMQSyncManager;
import org.openflexo.pamela.sync.SyncEditingContext;
import org.openflexo.pamela.sync.SyncOperation;
import org.openflexo.pamela.sync.SyncOperationListener;

/**
 * Test demonstrating collaborative synchronization of PAMELA objects
 * across multiple replicas using RabbitMQ.
 *
 * This test simulates two computers (Replica A and Replica B) working on
 * the same document. When Replica A modifies a property, Replica B should
 * receive the change and update its local instance.
 *
 * PREREQUISITES:
 * - RabbitMQ server must be running on localhost:5672
 * - Default guest/guest credentials (or configure as needed)
 *
 * To run RabbitMQ locally with Docker:
 *   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 *
 * @author PAMELA Sync Test
 */
public class CollaborativeDocumentSyncTest {

	// CloudAMQP configuration
	private static final String RABBITMQ_HOST = "rat.rmq2.cloudamqp.com";
	private static final int RABBITMQ_PORT = 5671;
	private static final String RABBITMQ_USERNAME = "gcyabtej";
	private static final String RABBITMQ_PASSWORD = "C91PisA-dAYuoVTxHRnzU1RCU1fERHeU";
	private static final String RABBITMQ_VHOST = "gcyabtej";
	private static final boolean USE_SSL = true;
	private static final String EXCHANGE_NAME = "pamela-sync-test";

	// Replica A (simulates Computer 1)
	private PamelaModelFactory factoryA;
	private SyncEditingContext contextA;
	private RabbitMQSyncManager syncManagerA;

	// Replica B (simulates Computer 2)
	private PamelaModelFactory factoryB;
	private SyncEditingContext contextB;
	private RabbitMQSyncManager syncManagerB;

	// Test synchronization helpers
	private List<SyncOperation> receivedOperationsA = new ArrayList<>();
	private List<SyncOperation> receivedOperationsB = new ArrayList<>();
	private CountDownLatch operationLatchB;

	@Before
	public void setUp() throws Exception {
		// Initialize Replica A
		factoryA = new PamelaModelFactory(CollaborativeDocument.class);
		contextA = new SyncEditingContext(factoryA);
		factoryA.setEditingContext(contextA);

		syncManagerA = RabbitMQSyncManager.builder()
				.host(RABBITMQ_HOST)
				.port(RABBITMQ_PORT)
				.credentials(RABBITMQ_USERNAME, RABBITMQ_PASSWORD)
				.virtualHost(RABBITMQ_VHOST)
				.useSsl(USE_SSL)
				.exchangeName(EXCHANGE_NAME)
				.build();

		// Initialize Replica B
		factoryB = new PamelaModelFactory(CollaborativeDocument.class);
		contextB = new SyncEditingContext(factoryB);
		factoryB.setEditingContext(contextB);

		syncManagerB = RabbitMQSyncManager.builder()
				.host(RABBITMQ_HOST)
				.port(RABBITMQ_PORT)
				.credentials(RABBITMQ_USERNAME, RABBITMQ_PASSWORD)
				.virtualHost(RABBITMQ_VHOST)
				.useSsl(USE_SSL)
				.exchangeName(EXCHANGE_NAME)
				.build();
	}

	@After
	public void tearDown() {
		if (syncManagerA != null) {
			syncManagerA.disconnect();
		}
		if (syncManagerB != null) {
			syncManagerB.disconnect();
		}
	}

	/**
	 * Test that creating an object on Replica A and modifying it
	 * propagates the changes to Replica B.
	 *
	 * Scenario:
	 * 1. Replica A creates a CollaborativeDocument
	 * 2. Replica A sets the title to "Hello from Computer A"
	 * 3. Replica B receives the SET operation
	 * 4. Replica B applies the change to its local instance
	 */
	@Test
	public void testSetterPropagation() throws Exception {
		// Skip if RabbitMQ is not available
		if (!isRabbitMQAvailable()) {
			System.out.println("SKIPPING TEST: RabbitMQ not available on " + RABBITMQ_HOST + ":" + RABBITMQ_PORT);
			return;
		}

		// Connect both replicas
		syncManagerA.connect();
		syncManagerB.connect();

		// Set up listeners
		contextA.setSyncManager(syncManagerA);
		syncManagerA.addListener(contextA);
		syncManagerA.addListener(new TestOperationListener(receivedOperationsA, null));

		contextB.setSyncManager(syncManagerB);
		syncManagerB.addListener(contextB);
		syncManagerB.addListener(new TestOperationListener(receivedOperationsB, null)); // Don't use latch yet

		// Give time for queues to be set up and flush any old messages
		Thread.sleep(1000);

		// NOW clear any stale operations
		receivedOperationsB.clear();
		receivedOperationsA.clear();

		// ========== REPLICA A: Create document ==========
		CollaborativeDocument docA = factoryA.newInstance(CollaborativeDocument.class);

		// Get the object ID assigned to docA
		String docId = contextA.getIdentityManager().getOrCreateObjectId(docA);
		assertNotNull("Document should have an ID", docId);
		System.out.println("[Replica A] Created document with ID: " + docId);

		// ========== REPLICA B: Create corresponding local instance ==========
		// In a real scenario, this would be done when receiving the CREATE operation
		CollaborativeDocument docB = factoryB.newInstance(CollaborativeDocument.class);
		contextB.getIdentityManager().registerObject(docB, docId);
		System.out.println("[Replica B] Registered local document with same ID: " + docId);

		// Now set up the countdown latch for the operations we care about
		operationLatchB = new CountDownLatch(2); // CREATE + SET
		syncManagerB.addListener(new TestOperationListener(null, operationLatchB));

		// ========== REPLICA A: Modify the document ==========
		System.out.println("[Replica A] Setting title to 'Hello from Computer A'");
		docA.setTitle("Hello from Computer A");

		// Wait for Replica B to receive both CREATE and SET operations
		boolean received = operationLatchB.await(5, TimeUnit.SECONDS);
		assertTrue("Replica B should have received the operations", received);

		// Small wait to ensure all operations are processed
		Thread.sleep(200);

		// Verify the operation was received
		assertFalse("Replica B should have received operations", receivedOperationsB.isEmpty());

		// Debug: print all received operations
		System.out.println("[DEBUG] Total operations received: " + receivedOperationsB.size());
		for (SyncOperation op : receivedOperationsB) {
			System.out.println("  - " + op.getOperationType() + " on objectId=" + op.getObjectId() + " property=" + op.getPropertyIdentifier());
		}

		// Find the SET operation for this specific document
		SyncOperation setOp = null;
		for (SyncOperation op : receivedOperationsB) {
			if (op.getOperationType() == SyncOperation.OperationType.SET
					&& "title".equals(op.getPropertyIdentifier())
					&& docId.equals(op.getObjectId())) {
				setOp = op;
				break;
			}
		}

		assertNotNull("Should have received a SET operation for title", setOp);
		System.out.println("[Replica B] Received operation: " + setOp.getOperationType()
				+ " on property '" + setOp.getPropertyIdentifier() + "'");

		assertEquals("Operation type should be SET", SyncOperation.OperationType.SET, setOp.getOperationType());
		assertEquals("Property should be 'title'", "title", setOp.getPropertyIdentifier());
		assertEquals("New value should match", "Hello from Computer A", setOp.getNewValueSerialized());

		// Verify Replica B's document was updated
		assertEquals("Replica B's document title should be updated",
				"Hello from Computer A", docB.getTitle());

		System.out.println("[Replica B] Local document title updated to: " + docB.getTitle());
		System.out.println("✓ Test passed: Setter propagation works!");
	}
	/**
	 * Test that adding items to a list on Replica A propagates to Replica B.
	 */
	@Test
	public void testAdderPropagation() throws Exception {
		if (!isRabbitMQAvailable()) {
			System.out.println("SKIPPING TEST: RabbitMQ not available");
			return;
		}

		// Expecting CREATE + 2 ADD operations
		operationLatchB = new CountDownLatch(3);

		// Connect replicas
		syncManagerA.connect();
		syncManagerB.connect();

		contextA.setSyncManager(syncManagerA);
		syncManagerA.addListener(contextA);

		contextB.setSyncManager(syncManagerB);
		syncManagerB.addListener(contextB);
		syncManagerB.addListener(new TestOperationListener(receivedOperationsB, operationLatchB));

		Thread.sleep(1000); // Increased wait time

		// Create document on Replica A ONLY
		CollaborativeDocument docA = factoryA.newInstance(CollaborativeDocument.class);
		String docId = contextA.getIdentityManager().getOrCreateObjectId(docA);

		System.out.println("[Replica A] Created document with ID: " + docId);

		// Wait for CREATE operation to propagate
		Thread.sleep(500);

		// NOW create the corresponding object on Replica B
		// This should ideally happen automatically when CREATE is received,
		// but for this test we do it manually
		CollaborativeDocument docB = factoryB.newInstance(CollaborativeDocument.class);
		contextB.getIdentityManager().registerObject(docB, docId);
		System.out.println("[Replica B] Registered local document with same ID: " + docId);

		// Reset latch for just the ADD operations
		operationLatchB = new CountDownLatch(2);
		syncManagerB.addListener(new TestOperationListener(receivedOperationsB, operationLatchB));

		// Add tags on Replica A
		System.out.println("[Replica A] Adding tags 'java' and 'pamela'");
		docA.addToTags("java");
		docA.addToTags("pamela");

		// Wait for ADD operations
		boolean received = operationLatchB.await(10, TimeUnit.SECONDS); // Increased timeout
		assertTrue("Replica B should have received ADD operations", received);

		// Debug: Print received operations
		System.out.println("[Replica B] Received " + receivedOperationsB.size() + " operations");
		for (SyncOperation op : receivedOperationsB) {
			System.out.println("  - " + op.getOperationType() + " on " + op.getPropertyIdentifier());
		}

		// Debug: Print current tags
		System.out.println("[Replica B] Current tags: " + docB.getTags());
		System.out.println("[Replica B] Tags size: " + docB.getTags().size());

		// Verify tags were added on Replica B
		assertTrue("Replica B should have 'java' tag", docB.getTags().contains("java"));
		assertTrue("Replica B should have 'pamela' tag", docB.getTags().contains("pamela"));

		System.out.println("[Replica B] Tags: " + docB.getTags());
		System.out.println("✓ Test passed: Adder propagation works!");
	}

	/**
	 * Test bidirectional synchronization - both replicas can make changes.
	 */
	@Test
	public void testBidirectionalSync() throws Exception {
		if (!isRabbitMQAvailable()) {
			System.out.println("SKIPPING TEST: RabbitMQ not available");
			return;
		}

		// Connect replicas
		syncManagerA.connect();
		syncManagerB.connect();

		contextA.setSyncManager(syncManagerA);
		syncManagerA.addListener(contextA);

		contextB.setSyncManager(syncManagerB);
		syncManagerB.addListener(contextB);

		Thread.sleep(1000);

		// ========== FIRST: Test A -> B (we know this works) ==========
		CollaborativeDocument docA = factoryA.newInstance(CollaborativeDocument.class);
		String docId = contextA.getIdentityManager().getOrCreateObjectId(docA);
		System.out.println("[Test] Created docA with ID: " + docId);

		Thread.sleep(500);

		CollaborativeDocument docB = factoryB.newInstance(CollaborativeDocument.class);
		contextB.getIdentityManager().registerObject(docB, docId);
		System.out.println("[Test] Registered docB with same ID");

		System.out.println("\n[TEST 1] A -> B: Setting content on A");
		docA.setContent("Content from A");
		Thread.sleep(1000);

		assertEquals("Content from A", docB.getContent());
		System.out.println("[✓] A -> B works");

		// ========== SECOND: Test B -> A (this is failing) ==========
		// The problem: docB was created BEFORE syncManagerB was attached,
		// so changes to docB aren't being tracked!

		// Let's try creating a FRESH document on B AFTER sync is set up
		System.out.println("\n[TEST 2] B -> A: Creating NEW document on B");

		CollaborativeDocument docB2 = factoryB.newInstance(CollaborativeDocument.class);
		String docId2 = contextB.getIdentityManager().getOrCreateObjectId(docB2);
		System.out.println("[Test] Created docB2 with ID: " + docId2);

		Thread.sleep(500);

		// Register corresponding object on A
		CollaborativeDocument docA2 = factoryA.newInstance(CollaborativeDocument.class);
		contextA.getIdentityManager().registerObject(docA2, docId2);
		System.out.println("[Test] Registered docA2 with same ID");

		// NOW try to modify docB2
		System.out.println("[Test] Setting author on docB2");
		docB2.setAuthor("User from B");
		Thread.sleep(2000);

		System.out.println("[Debug] docA2.getAuthor() = " + docA2.getAuthor());
		System.out.println("[Debug] docB2.getAuthor() = " + docB2.getAuthor());

		assertEquals("User from B", docA2.getAuthor());
		System.out.println("[✓] B -> A works");

		System.out.println("\n✓ Bidirectional sync works!");
	}
	/**
	 * Check if RabbitMQ is available for testing.
	 */
	private boolean isRabbitMQAvailable() {
		try {
			RabbitMQSyncManager testManager = RabbitMQSyncManager.builder()
					.host(RABBITMQ_HOST)
					.port(RABBITMQ_PORT)
					.credentials(RABBITMQ_USERNAME, RABBITMQ_PASSWORD)
					.virtualHost(RABBITMQ_VHOST)
					.useSsl(USE_SSL)
					.build();
			testManager.connect();
			testManager.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Helper listener to capture operations for test verification.
	 */
	private static class TestOperationListener implements SyncOperationListener {
		private final List<SyncOperation> operations;
		private final CountDownLatch latch;

		public TestOperationListener(List<SyncOperation> operations, CountDownLatch latch) {
			this.operations = operations;
			this.latch = latch;
		}

		@Override
		public void onOperationReceived(SyncOperation operation) {
			if (operations != null) {
				operations.add(operation);
			}
			if (latch != null) {
				latch.countDown();
			}
		}

		@Override
		public void onConnected() {
			System.out.println("  [Listener] Connected to RabbitMQ");
		}

		@Override
		public void onDisconnected(String reason) {
			System.out.println("  [Listener] Disconnected from RabbitMQ: " + reason);
		}

		@Override
		public void onError(Throwable e) {
			System.err.println("  [Listener] Error: " + e.getMessage());
		}
	}
}
