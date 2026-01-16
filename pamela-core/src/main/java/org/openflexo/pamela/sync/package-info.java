/**
 * PAMELA Synchronization Package
 * 
 * This package provides real-time collaborative editing capabilities for PAMELA models
 * through RabbitMQ message broker integration.
 * 
 * <h2>Key Components:</h2>
 * <ul>
 *   <li>{@link org.openflexo.pamela.sync.SyncEditingContext} - Main entry point for synchronized editing</li>
 *   <li>{@link org.openflexo.pamela.sync.RabbitMQSyncManager} - Handles RabbitMQ communication</li>
 *   <li>{@link org.openflexo.pamela.sync.SyncOperation} - Represents a synchronizable operation</li>
 *   <li>{@link org.openflexo.pamela.sync.VectorClock} - Tracks causality between operations</li>
 *   <li>{@link org.openflexo.pamela.sync.ObjectIdentityManager} - Manages object UUIDs across replicas</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create a synchronized editing context
 * SyncEditingContext syncContext = new SyncEditingContext();
 * 
 * // Configure RabbitMQ connection (optional, uses localhost by default)
 * // RabbitMQSyncManager customManager = new RabbitMQSyncManager.Builder()
 * //     .host("rabbitmq.example.com")
 * //     .credentials("user", "password")
 * //     .build();
 * // SyncEditingContext syncContext = new SyncEditingContext(customManager);
 * 
 * // Create a model factory with the sync context
 * PamelaModelFactory factory = new PamelaModelFactory(MyModel.class);
 * factory.setEditingContext(syncContext);
 * syncContext.setModelFactory(factory);
 * 
 * // Connect to RabbitMQ
 * syncContext.connect();
 * 
 * // Now all operations on PAMELA objects will be synchronized
 * MyModel model = factory.newInstance(MyModel.class);
 * model.setName("Hello"); // This is broadcast to all other replicas
 * 
 * // Disconnect when done
 * syncContext.disconnect();
 * }</pre>
 * 
 * @author PAMELA Team
 * @since 2024
 */
package org.openflexo.pamela.sync;
