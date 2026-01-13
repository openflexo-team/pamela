/*
 * Replica A - Collaborative Library Example
 * 
 * This application demonstrates PAMELA distributed synchronization.
 * Run this first, then start ReplicaB and enter the Library ID.
 * After ReplicaB is connected, add books and see them sync!
 * 
 * Prerequisites:
 *   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
 */
package org.openflexo.testPamela;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.sync.RabbitMQSyncManager;
import org.openflexo.pamela.sync.SyncEditingContext;
import org.openflexo.pamela.sync.SyncOperation;
import org.openflexo.pamela.sync.SyncOperationListener;
import org.openflexo.testPamela.model.Book;
import org.openflexo.testPamela.model.Library;

import java.util.Scanner;

public class ReplicaA {

    private static Library library;
    private static PamelaModelFactory factory;

    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           PAMELA Distributed Library - REPLICA A           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Instantiate the meta-model
        PamelaMetaModel pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(Library.class);
        
        // Instantiate the factory with sync context
        factory = new PamelaModelFactory(pamelaMetaModel);
        SyncEditingContext syncContext = new SyncEditingContext(factory);
        factory.setEditingContext(syncContext);

        // Configure RabbitMQ sync manager (CloudAMQP)
        System.out.println("[A] Connecting to CloudAMQP...");
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
        
        // Add a listener to show received operations from ReplicaB
        syncManager.addListener(new SyncOperationListener() {
            @Override
            public void onOperationReceived(SyncOperation operation) {
                System.out.println();
                System.out.println("[A] 📥 Received from remote: " + operation.getOperationType() 
                        + " on " + operation.getPropertyIdentifier()
                        + " value=" + operation.getNewValueSerialized());
                System.out.println("[A] Current library books: " + library.getBooks().size());
                System.out.print("[A] > ");
            }
        });

        try {
            syncManager.connect();
            System.out.println("[A] ✓ Connected to RabbitMQ as replica: " + syncManager.getReplicaId().substring(0, 8));
        } catch (Exception e) {
            System.err.println("[A] ✗ Failed to connect to RabbitMQ: " + e.getMessage());
            System.err.println("[A] Make sure RabbitMQ is running: docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management");
            return;
        }

        // Create a library (no initial books - wait for ReplicaB to connect first!)
        library = factory.newInstance(Library.class);
        String libraryId = syncContext.getIdentityManager().getOrCreateObjectId(library);
        System.out.println();
        System.out.println("[A] Created Library with ID: " + libraryId);
        System.out.println("    ⚠️  COPY THIS ID to use in ReplicaB!");
        System.out.println();
        System.out.println("    ➡️  Start ReplicaB now, enter the ID, then add books here!");
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("Commands: 'add <title>' | 'remove <title>' | 'list' | 'quit'");
        System.out.println("════════════════════════════════════════════════════════════════");

        // Interactive mode
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[A] > ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                break;
            } else if (input.equalsIgnoreCase("list")) {
                System.out.println("[A] Library: " + library);
                System.out.println("[A] Books count: " + library.getBooks().size());
                for (Book b : library.getBooks()) {
                    System.out.println("    - " + b.getTitle() + " (ISBN: " + b.getISBN() + ")");
                }
            } else if (input.toLowerCase().startsWith("add ")) {
                String title = input.substring(4).trim();
                if (!title.isEmpty()) {
                    Book newBook = factory.newInstance(Book.class, title);
                    newBook.setISBN("A-" + System.currentTimeMillis());
                    library.addToBooks(newBook);
                    System.out.println("[A] ✓ Added: " + newBook.getTitle() + " (broadcasting to replicas...)");
                }
            } else if (input.toLowerCase().startsWith("remove ")) {
                String title = input.substring(7).trim();
                Book bookToRemove = library.getBook(title);
                if (bookToRemove != null) {
                    library.removeFromBooks(bookToRemove);
                    System.out.println("[A] ✓ Removed: " + title);
                } else {
                    System.out.println("[A] ✗ Book not found: " + title);
                }
            } else if (!input.isEmpty()) {
                System.out.println("[A] Unknown command. Use: add <title>, remove <title>, list, quit");
            }
        }

        System.out.println("[A] Disconnecting...");
        syncManager.disconnect();
        System.out.println("[A] Goodbye!");
    }
}
