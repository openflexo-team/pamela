/*
 * Replica C - Collaborative Library Example
 * 
 * This application demonstrates PAMELA distributed synchronization.
 * Run this AFTER ReplicaA to receive synchronized updates (3rd replica).
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

public class ReplicaC {

    private static Library library;
    private static SyncEditingContext syncContext;
    private static PamelaModelFactory factory;

    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           PAMELA Distributed Library - REPLICA C           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Instantiate the meta-model
        PamelaMetaModel pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(Library.class);
        
        // Instantiate the factory with sync context
        factory = new PamelaModelFactory(pamelaMetaModel);
        syncContext = new SyncEditingContext(factory);
        factory.setEditingContext(syncContext);

        // Configure RabbitMQ sync manager (CloudAMQP)
        System.out.println("[C] Connecting to CloudAMQP...");
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
                System.out.println("[C] 📥 Received: " + operation.getOperationType() 
                        + " on " + operation.getPropertyIdentifier()
                        + " = " + operation.getNewValueSerialized());
                System.out.println("[C] Current library: " + library);
                System.out.print("[C] > ");
            }
        });

        try {
            syncManager.connect();
            System.out.println("[C] ✓ Connected to RabbitMQ as replica: " + syncManager.getReplicaId().substring(0, 8));
        } catch (Exception e) {
            System.err.println("[C] ✗ Failed to connect to RabbitMQ: " + e.getMessage());
            System.err.println("[C] Make sure RabbitMQ is running!");
            return;
        }

        System.out.println();
        System.out.println("[C] Waiting for Library ID from ReplicaA...");
        System.out.print("[C] Enter Library ID (from ReplicaA): ");
        
        Scanner scanner = new Scanner(System.in);
        String libraryId = scanner.nextLine().trim();
        
        if (libraryId.isEmpty()) {
            // Create a new library if no ID provided
            library = factory.newInstance(Library.class);
            libraryId = syncContext.getIdentityManager().getOrCreateObjectId(library);
            System.out.println("[C] Created new Library with ID: " + libraryId);
        } else {
            // Create a local library and register with the same ID
            library = factory.newInstance(Library.class);
            syncContext.getIdentityManager().registerObject(library, libraryId);
            System.out.println("[C] ✓ Registered local Library with shared ID: " + libraryId);
        }

        System.out.println();
        System.out.println("[C] Library state: " + library);
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("Commands: 'add <title>' | 'remove <title>' | 'list' | 'quit'");
        System.out.println("Waiting for updates from ReplicaA...");
        System.out.println("════════════════════════════════════════════════════════════════");

        // Interactive mode
        while (true) {
            System.out.print("[C] > ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            } else if (input.equalsIgnoreCase("list")) {
                System.out.println("[C] Library: " + library);
                System.out.println("[C] Books count: " + library.getBooks().size());
                for (Book b : library.getBooks()) {
                    System.out.println("    - " + b.getTitle() + " (ISBN: " + b.getISBN() + ")");
                }
            } else if (input.toLowerCase().startsWith("add ")) {
                String title = input.substring(4).trim();
                if (!title.isEmpty()) {
                    Book newBook = factory.newInstance(Book.class, title);
                    newBook.setISBN("NEW-C-" + System.currentTimeMillis());
                    library.addToBooks(newBook);
                    System.out.println("[C] ✓ Added: " + newBook);
                }
            } else if (input.toLowerCase().startsWith("remove ")) {
                String title = input.substring(7).trim();
                Book bookToRemove = library.getBook(title);
                if (bookToRemove != null) {
                    library.removeFromBooks(bookToRemove);
                    System.out.println("[C] ✓ Removed: " + title);
                } else {
                    System.out.println("[C] ✗ Book not found: " + title);
                }
            } else if (!input.isEmpty()) {
                System.out.println("[C] Unknown command. Use: add <title>, remove <title>, list, quit");
            }
        }

        System.out.println("[C] Disconnecting...");
        syncManager.disconnect();
        System.out.println("[C] Goodbye!");
    }
}
