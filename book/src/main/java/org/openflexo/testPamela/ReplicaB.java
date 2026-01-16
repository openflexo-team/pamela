/*
 * Replica B - Collaborative Library Example
 * 
 * This application demonstrates PAMELA distributed synchronization.
 * Run this AFTER ReplicaA to receive synchronized updates.
 * 
 * Prerequisites:
 *   1. RabbitMQ running: docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 *   2. ReplicaA running and note the Library ID
 */
package org.openflexo.testPamela;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.sync.RabbitMQSyncManager;
import org.openflexo.pamela.sync.SyncEditingContext;
import org.openflexo.pamela.sync.SyncOperation;
import org.openflexo.pamela.sync.SyncOperationListener;
import org.openflexo.testPamela.model.Book;
import org.openflexo.testPamela.model.Library;

import java.util.Scanner;

public class ReplicaB {

    private static Library library;
    private static SyncEditingContext syncContext;
    private static PamelaModelFactory factory;

    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           PAMELA Distributed Library - REPLICA B           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Instantiate the meta-model
        PamelaMetaModel pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(Library.class);
        
        // Instantiate the factory with sync context
        factory = new PamelaModelFactory(pamelaMetaModel);
        syncContext = new SyncEditingContext(factory);
        factory.setEditingContext(syncContext);

        // Configure RabbitMQ sync manager (CloudAMQP)
        System.out.println("[B] Connecting to CloudAMQP...");
        RabbitMQSyncManager syncManager = RabbitMQSyncManager.builder()
                .host("rat.rmq2.cloudamqp.com")
                .port(5671)
                .credentials("gcyabtej", "C91PisA-dAYuoVTxHRnzU1RCU1fERHeU")
                .virtualHost("gcyabtej")
                .useSsl(true)
                .exchangeName("pamela-library-sync")
                .build();

        syncContext.setSyncManager(syncManager);
        syncManager.addListener(syncContext);
        
        // Add a listener to show received operations
        syncManager.addListener(new SyncOperationListener() {
            @Override
            public void onOperationReceived(SyncOperation operation) {
                System.out.println();
                System.out.println("[B] 📥 Received: " + operation.getOperationType() 
                        + " on " + operation.getPropertyIdentifier()
                        + " = " + operation.getNewValueSerialized());
                System.out.println("[B] Current library: " + library);
                System.out.print("[B] > ");
            }
        });

        try {
            syncManager.connect();
            System.out.println("[B] ✓ Connected to RabbitMQ as replica: " + syncManager.getReplicaId().substring(0, 8));
        } catch (Exception e) {
            System.err.println("[B] ✗ Failed to connect to RabbitMQ: " + e.getMessage());
            System.err.println("[B] Make sure RabbitMQ is running!");
            return;
        }

        System.out.println();
        System.out.println("[B] Waiting for Library ID from ReplicaA...");
        System.out.print("[B] Enter Library ID (from ReplicaA): ");
        
        Scanner scanner = new Scanner(System.in);
        String libraryId = scanner.nextLine().trim();
        
        if (libraryId.isEmpty()) {
            // Create a new library if no ID provided
            library = factory.newInstance(Library.class);
            libraryId = syncContext.getIdentityManager().getOrCreateObjectId(library);
            System.out.println("[B] Created new Library with ID: " + libraryId);
        } else {
            // Create a local library and register with the same ID
            library = factory.newInstance(Library.class);
            syncContext.getIdentityManager().registerObject(library, libraryId);
            System.out.println("[B] ✓ Registered local Library with shared ID: " + libraryId);
            
            // Request state from other replicas to get existing books
            System.out.println("[B] 📡 Requesting state from other replicas...");
            syncContext.requestStateSync();
            
            // Wait a moment for state to arrive
            Thread.sleep(2000);
            System.out.println("[B] ✓ State sync complete. Books received: " + library.getBooks().size());
        }

        System.out.println();
        System.out.println("[B] Library state: " + library);
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("Commands: 'add <title>' | 'remove <title>' | 'list' | 'quit'");
        System.out.println("Waiting for updates from ReplicaA...");
        System.out.println("════════════════════════════════════════════════════════════════");

        // Interactive mode
        while (true) {
            System.out.print("[B] > ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            } else if (input.equalsIgnoreCase("list")) {
                System.out.println("[B] Library: " + library);
                System.out.println("[B] Books count: " + library.getBooks().size());
                for (Book b : library.getBooks()) {
                    System.out.println("    - " + b.getTitle() + " (ISBN: " + b.getISBN() + ")");
                }
            } else if (input.toLowerCase().startsWith("add ")) {
                String title = input.substring(4).trim();
                if (!title.isEmpty()) {
                    Book newBook = factory.newInstance(Book.class, title);
                    newBook.setISBN("NEW-B-" + System.currentTimeMillis());
                    library.addToBooks(newBook);
                    System.out.println("[B] ✓ Added: " + newBook);
                }
            } else if (input.toLowerCase().startsWith("remove ")) {
                String title = input.substring(7).trim();
                Book bookToRemove = library.getBook(title);
                if (bookToRemove != null) {
                    library.removeFromBooks(bookToRemove);
                    System.out.println("[B] ✓ Removed: " + title);
                } else {
                    System.out.println("[B] ✗ Book not found: " + title);
                }
            } else if (!input.isEmpty()) {
                System.out.println("[B] Unknown command. Use: add <title>, remove <title>, list, quit");
            }
        }

        System.out.println("[B] Disconnecting...");
        syncManager.disconnect();
        System.out.println("[B] Goodbye!");
    }
}
