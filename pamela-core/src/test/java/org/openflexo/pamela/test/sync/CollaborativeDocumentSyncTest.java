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

		// Expecting CREATE + SET operations
		operationLatchB = new CountDownLatch(2);

		// Connect both replicas
		syncManagerA.connect();
		syncManagerB.connect();

		// Set up listeners
		contextA.setSyncManager(syncManagerA);
		syncManagerA.addListener(contextA);
		syncManagerA.addListener(new TestOperationListener(receivedOperationsA, null));

		contextB.setSyncManager(syncManagerB);
		syncManagerB.addListener(contextB);
		syncManagerB.addListener(new TestOperationListener(receivedOperationsB, operationLatchB));

		// Give time for queues to be set up
		Thread.sleep(500);

		// ========== REPLICA A: Create document ==========
		CollaborativeDocument docA = factoryA.newInstance(CollaborativeDocument.class);
		
		// Get the object ID assigned to docA
		String docId = contextA.getIdentityManager().getOrCreateObjectId(docA);
		assertNotNull("Document should have an ID", docId);
		System.out.println("[Replica A] Created document with ID: " + docId);

		// ========== REPLICA A: Modify the document ==========
		System.out.println("[Replica A] Setting title to 'Hello from Computer A'");
		docA.setTitle("Hello from Computer A");

		// Wait for Replica B to receive both CREATE and SET operations
		boolean received = operationLatchB.await(5, TimeUnit.SECONDS);
		assertTrue("Replica B should have received the operations", received);

		// Verify the operation was received
		assertFalse("Replica B should have received operations", receivedOperationsB.isEmpty());
		
		// Find the SET operation (last one should be SET)
		SyncOperation receivedOp = receivedOperationsB.get(receivedOperationsB.size() - 1);
		System.out.println("[Replica B] Received operation: " + receivedOp.getOperationType() 
				+ " on property '" + receivedOp.getPropertyIdentifier() + "'");
		
		assertEquals("Operation type should be SET", SyncOperation.OperationType.SET, receivedOp.getOperationType());
		assertEquals("Property should be 'title'", "title", receivedOp.getPropertyIdentifier());
		assertEquals("New value should match", "Hello from Computer A", receivedOp.getNewValueSerialized());

		// Get the object created on Replica B by the remote CREATE operation
		CollaborativeDocument docB = (CollaborativeDocument) contextB.getIdentityManager().getObject(docId);
		assertNotNull("Replica B should have created the document via applyRemoteCreate", docB);
		
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

		Thread.sleep(500);

		// Create document on Replica A
		CollaborativeDocument docA = factoryA.newInstance(CollaborativeDocument.class);
		String docId = contextA.getIdentityManager().getOrCreateObjectId(docA);
		System.out.println("[Replica A] Created document with ID: " + docId);

		// Add tags on Replica A
		System.out.println("[Replica A] Adding tags 'java' and 'pamela'");
		docA.addToTags("java");
		docA.addToTags("pamela");

		// Wait for operations (CREATE + 2 ADD)
		boolean received = operationLatchB.await(5, TimeUnit.SECONDS);
		assertTrue("Replica B should have received CREATE + ADD operations", received);

		// Get the object created on Replica B by the remote CREATE operation
		CollaborativeDocument docB = (CollaborativeDocument) contextB.getIdentityManager().getObject(docId);
		assertNotNull("Replica B should have created the document via applyRemoteCreate", docB);

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

		// Expecting CREATE + SET from Replica A
		operationLatchB = new CountDownLatch(2);

		// Connect replicas
		syncManagerA.connect();
		syncManagerB.connect();

		contextA.setSyncManager(syncManagerA);
		syncManagerA.addListener(contextA);
		syncManagerA.addListener(new TestOperationListener(receivedOperationsA, null));

		contextB.setSyncManager(syncManagerB);
		syncManagerB.addListener(contextB);
		syncManagerB.addListener(new TestOperationListener(receivedOperationsB, operationLatchB));

		Thread.sleep(500);

		// Replica A creates document and sets content
		// Replica B will automatically receive CREATE and create its local instance
		CollaborativeDocument docA = factoryA.newInstance(CollaborativeDocument.class);
		String docId = contextA.getIdentityManager().getOrCreateObjectId(docA);
		System.out.println("[Replica A] Created document with ID: " + docId);

		// Replica A modifies content
		System.out.println("[Replica A] Setting content");
		docA.setContent("Content from A");
		
		// Wait for Replica B to receive CREATE + SET
		boolean received = operationLatchB.await(5, TimeUnit.SECONDS);
		assertTrue("Replica B should have received CREATE + SET", received);

		// Get the object created on Replica B by the remote CREATE operation
		CollaborativeDocument docB = (CollaborativeDocument) contextB.getIdentityManager().getObject(docId);
		assertNotNull("Replica B should have created the document via applyRemoteCreate", docB);
		assertEquals("Content from A", docB.getContent());
		System.out.println("[Replica B] Document received with content: " + docB.getContent());

		// Reset latch for Replica B -> Replica A direction (expecting only SET)
		CountDownLatch latchA = new CountDownLatch(1);
		syncManagerA.addListener(new TestOperationListener(null, latchA));

		// Replica B modifies author
		System.out.println("[Replica B] Setting author");
		docB.setAuthor("User B");

		received = latchA.await(5, TimeUnit.SECONDS);
		assertTrue("Replica A should have received SET from Replica B", received);
		assertEquals("User B", docA.getAuthor());

		System.out.println("[Result] docA: content='" + docA.getContent() + "', author='" + docA.getAuthor() + "'");
		System.out.println("[Result] docB: content='" + docB.getContent() + "', author='" + docB.getAuthor() + "'");
		System.out.println("✓ Test passed: Bidirectional sync works!");
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
