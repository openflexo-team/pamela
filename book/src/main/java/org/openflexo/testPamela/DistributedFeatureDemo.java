/*
 * PAMELA Distributed Features Demo
 * 
 * This application demonstrates ALL distributed synchronization features:
 * 
 * 1. STATE_REQUEST / STATE_RESPONSE - State sync for new clients
 * 2. CREATE - Object creation synchronization
 * 3. SET - Property setter synchronization  
 * 4. ADD - Collection adder synchronization
 * 5. REMOVE - Collection remover synchronization
 * 6. DELETE - Object deletion synchronization
 * 7. VectorClock - Causality tracking
 * 8. Auto state request on connect
 * 
 * Run multiple instances to see real-time synchronization!
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
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedFeatureDemo {

    private static Library library;
    private static PamelaModelFactory factory;
    private static SyncEditingContext syncContext;
    private static String replicaName;
    private static final AtomicInteger bookCounter = new AtomicInteger(1);

    // CloudAMQP configuration
    private static final String AMQP_HOST = "rat.rmq2.cloudamqp.com";
    private static final int AMQP_PORT = 5671;
    private static final String AMQP_USER = "gcyabtej";
    private static final String AMQP_PASS = "C91PisA-dAYuoVTxHRnzU1RCU1fERHeU";
    private static final String AMQP_VHOST = "gcyabtej";

    public static void main(String[] args) throws Exception {
        printBanner();
        
        Scanner scanner = new Scanner(System.in);
        
        // Get replica name
        System.out.print("Enter your replica name (e.g., Alice, Bob, Charlie): ");
        replicaName = scanner.nextLine().trim();
        if (replicaName.isEmpty()) {
            replicaName = "Replica-" + System.currentTimeMillis() % 1000;
        }
        
        System.out.println();
        System.out.println("[" + replicaName + "] Initializing PAMELA...");
        
        // Initialize PAMELA
        PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(Library.class);
        factory = new PamelaModelFactory(metaModel);
        syncContext = new SyncEditingContext(factory);
        factory.setEditingContext(syncContext);

        // Configure RabbitMQ sync manager
        System.out.println("[" + replicaName + "] Connecting to CloudAMQP...");
        RabbitMQSyncManager syncManager = RabbitMQSyncManager.builder()
                .host(AMQP_HOST)
                .port(AMQP_PORT)
                .credentials(AMQP_USER, AMQP_PASS)
                .virtualHost(AMQP_VHOST)
                .useSsl(true)
                .exchangeName("pamela-distributed-demo")
                .build();

        // Setup sync context with auto state request
        syncContext.setSyncManager(syncManager);
        syncManager.addListener(syncContext);
        
        // Add operation listener for visibility
        syncManager.addListener(createOperationListener());

        try {
            syncManager.connect();
            System.out.println("[" + replicaName + "] ✓ Connected! Replica ID: " + 
                    syncManager.getReplicaId().substring(0, 8));
        } catch (Exception e) {
            System.err.println("[" + replicaName + "] ✗ Connection failed: " + e.getMessage());
            return;
        }

        // Ask if joining existing session or creating new
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  1. CREATE new library (if you're the first replica)          ║");
        System.out.println("║  2. JOIN existing library (enter Library ID from another)     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.print("Choice [1/2]: ");
        
        String choice = scanner.nextLine().trim();
        
        if ("2".equals(choice)) {
            // Join existing library
            System.out.print("Enter Library ID: ");
            String libraryId = scanner.nextLine().trim();
            
            if (!libraryId.isEmpty()) {
                // Create local library and register with same ID
                library = factory.newInstance(Library.class);
                syncContext.getIdentityManager().registerObject(library, libraryId);
                System.out.println("[" + replicaName + "] Registered with Library ID: " + libraryId);
                
                // Request state from other replicas (KEY FEATURE: STATE_REQUEST)
                System.out.println("[" + replicaName + "] 📡 Requesting current state from other replicas...");
                syncContext.requestStateSync();
                
                // Wait for state to arrive
                Thread.sleep(3000);
                System.out.println("[" + replicaName + "] ✓ State sync complete!");
                System.out.println("[" + replicaName + "] Books received: " + library.getBooks().size());
                printLibrary();
            } else {
                createNewLibrary();
            }
        } else {
            createNewLibrary();
        }

        // Print commands help
        printHelp();

        // Interactive command loop
        while (true) {
            System.out.print("[" + replicaName + "] > ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argument = parts.length > 1 ? parts[1] : "";
            
            try {
                switch (command) {
                    case "help":
                        printHelp();
                        break;
                        
                    case "add":
                        // TEST: ADD operation
                        testAddOperation(argument);
                        break;
                        
                    case "remove":
                        // TEST: REMOVE operation
                        testRemoveOperation(argument);
                        break;
                        
                    case "set":
                        // TEST: SET operation
                        testSetOperation(argument);
                        break;
                        
                    case "move":
                        // TEST: REINDEX operation
                        testMoveOperation(argument);
                        break;
                        
                    case "list":
                        printLibrary();
                        break;
                        
                    case "state":
                        // TEST: STATE_REQUEST
                        System.out.println("[" + replicaName + "] Requesting state sync...");
                        syncContext.requestStateSync();
                        break;
                        
                    case "info":
                        printInfo(syncManager);
                        break;
                        
                    case "quit":
                    case "exit":
                        System.out.println("[" + replicaName + "] Disconnecting...");
                        syncManager.disconnect();
                        System.out.println("[" + replicaName + "] Goodbye!");
                        return;
                        
                    case "demo":
                        runFullDemo();
                        break;
                        
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println("[" + replicaName + "] Error: " + e.getMessage());
            }
        }
    }

    private static void createNewLibrary() {
        library = factory.newInstance(Library.class);
        String libraryId = syncContext.getIdentityManager().getOrCreateObjectId(library);
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  NEW LIBRARY CREATED                                          ║");
        System.out.println("║  Library ID: " + libraryId);
        System.out.println("║                                                               ║");
        System.out.println("║  Share this ID with other replicas to join!                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * TEST: ADD operation - adds a book to the library
     */
    private static void testAddOperation(String title) {
        if (title.isEmpty()) {
            title = "Book-" + replicaName + "-" + bookCounter.getAndIncrement();
        }
        
        Book book = factory.newInstance(Book.class, title);
        book.setISBN(replicaName + "-" + System.currentTimeMillis());
        book.setPages(100 + (int)(Math.random() * 400));
        
        library.addToBooks(book);
        
        System.out.println("[" + replicaName + "] ✓ ADD: Created and added '" + title + "'");
        System.out.println("    → This triggers CREATE + ADD operations broadcast to all replicas");
    }

    /**
     * TEST: REMOVE operation - removes a book from the library
     */
    private static void testRemoveOperation(String title) {
        if (title.isEmpty()) {
            System.out.println("Usage: remove <book title>");
            return;
        }
        
        Book book = library.getBook(title);
        if (book != null) {
            library.removeFromBooks(book);
            System.out.println("[" + replicaName + "] ✓ REMOVE: Removed '" + title + "'");
            System.out.println("    → This triggers REMOVE operation broadcast to all replicas");
        } else {
            System.out.println("[" + replicaName + "] ✗ Book not found: " + title);
            System.out.println("    Available books:");
            for (Book b : library.getBooks()) {
                System.out.println("      - " + b.getTitle());
            }
        }
    }

    /**
     * TEST: SET operation - modifies a book's property
     */
    private static void testSetOperation(String args) {
        // Format: set <title> <property> <value>
        // Example: set MyBook pages 500
        String[] parts = args.split("\\s+", 3);
        if (parts.length < 3) {
            System.out.println("Usage: set <book title> <property> <value>");
            System.out.println("  Properties: title, isbn, pages");
            System.out.println("  Example: set MyBook pages 500");
            return;
        }
        
        String title = parts[0];
        String property = parts[1].toLowerCase();
        String value = parts[2];
        
        Book book = library.getBook(title);
        if (book == null) {
            System.out.println("[" + replicaName + "] ✗ Book not found: " + title);
            return;
        }
        
        switch (property) {
            case "title":
                book.setTitle(value);
                System.out.println("[" + replicaName + "] ✓ SET: Changed title to '" + value + "'");
                break;
            case "isbn":
                book.setISBN(value);
                System.out.println("[" + replicaName + "] ✓ SET: Changed ISBN to '" + value + "'");
                break;
            case "pages":
                book.setPages(Integer.parseInt(value));
                System.out.println("[" + replicaName + "] ✓ SET: Changed pages to " + value);
                break;
            default:
                System.out.println("Unknown property: " + property);
                return;
        }
        System.out.println("    → This triggers SET operation broadcast to all replicas");
    }

    /**
     * TEST: REINDEX operation - moves a book to a different position
     */
    private static void testMoveOperation(String args) {
        // Format: move <title> <new index>
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: move <book title> <new index>");
            System.out.println("  Example: move MyBook 0   (moves to first position)");
            return;
        }
        
        String title = parts[0];
        int newIndex = Integer.parseInt(parts[1]);
        
        Book book = library.getBook(title);
        if (book == null) {
            System.out.println("[" + replicaName + "] ✗ Book not found: " + title);
            return;
        }
        
        library.moveBookToIndex(book, newIndex);
        System.out.println("[" + replicaName + "] ✓ MOVE: Moved '" + title + "' to index " + newIndex);
        System.out.println("    → This triggers REINDEX operation broadcast to all replicas");
    }

    /**
     * Run a full demo of all features
     */
    private static void runFullDemo() throws InterruptedException {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  RUNNING FULL DISTRIBUTED FEATURES DEMO                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // 1. ADD - Create and add books
        System.out.println("▶ STEP 1: Testing ADD operation (CREATE + ADD)");
        testAddOperation("Demo-Book-1");
        Thread.sleep(500);
        testAddOperation("Demo-Book-2");
        Thread.sleep(500);
        testAddOperation("Demo-Book-3");
        Thread.sleep(1000);
        printLibrary();
        
        // 2. SET - Modify properties
        System.out.println();
        System.out.println("▶ STEP 2: Testing SET operation");
        testSetOperation("Demo-Book-1 pages 999");
        Thread.sleep(500);
        testSetOperation("Demo-Book-2 isbn DEMO-ISBN-123");
        Thread.sleep(1000);
        printLibrary();
        
        // 3. MOVE - Reorder
        System.out.println();
        System.out.println("▶ STEP 3: Testing REINDEX/MOVE operation");
        testMoveOperation("Demo-Book-3 0");
        Thread.sleep(1000);
        printLibrary();
        
        // 4. REMOVE - Delete
        System.out.println();
        System.out.println("▶ STEP 4: Testing REMOVE operation");
        testRemoveOperation("Demo-Book-2");
        Thread.sleep(1000);
        printLibrary();
        
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  DEMO COMPLETE!                                               ║");
        System.out.println("║                                                               ║");
        System.out.println("║  If you have another replica connected, it should now have    ║");
        System.out.println("║  received all these operations in real-time!                  ║");
        System.out.println("║                                                               ║");
        System.out.println("║  Try 'state' command on a new replica to test STATE_SYNC     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printLibrary() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ LIBRARY CONTENTS (" + library.getBooks().size() + " books)");
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        
        int idx = 0;
        for (Book book : library.getBooks()) {
            System.out.printf("│ [%d] %-20s ISBN: %-20s Pages: %d%n", 
                    idx++, 
                    truncate(book.getTitle(), 20),
                    truncate(book.getISBN(), 20),
                    book.getPages());
        }
        
        if (library.getBooks().isEmpty()) {
            System.out.println("│ (empty)                                                         │");
        }
        
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private static void printInfo(RabbitMQSyncManager syncManager) {
        System.out.println();
        System.out.println("┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│ SYNC INFO                                                       │");
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        System.out.println("│ Replica Name: " + replicaName);
        System.out.println("│ Replica ID:   " + syncManager.getReplicaId());
        System.out.println("│ Connected:    " + syncManager.isConnected());
        System.out.println("│ State Recv:   " + syncContext.isStateReceived());
        System.out.println("│ Objects:      " + syncContext.getIdentityManager().size());
        System.out.println("│ Vector Clock: " + syncManager.getVectorClock());
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  DISTRIBUTED PAMELA COMMANDS                                  ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        System.out.println("║  add [title]          - Add a new book (tests ADD + CREATE)   ║");
        System.out.println("║  remove <title>       - Remove a book (tests REMOVE)          ║");
        System.out.println("║  set <title> <prop> <val> - Modify property (tests SET)       ║");
        System.out.println("║  move <title> <index> - Reorder book (tests REINDEX)          ║");
        System.out.println("║  list                 - Show all books                        ║");
        System.out.println("║  state                - Request state from replicas           ║");
        System.out.println("║  info                 - Show sync information                 ║");
        System.out.println("║  demo                 - Run full feature demonstration        ║");
        System.out.println("║  quit                 - Exit                                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                       ║");
        System.out.println("║   ██████╗  █████╗ ███╗   ███╗███████╗██╗      █████╗                  ║");
        System.out.println("║   ██╔══██╗██╔══██╗████╗ ████║██╔════╝██║     ██╔══██╗                 ║");
        System.out.println("║   ██████╔╝███████║██╔████╔██║█████╗  ██║     ███████║                 ║");
        System.out.println("║   ██╔═══╝ ██╔══██║██║╚██╔╝██║██╔══╝  ██║     ██╔══██║                 ║");
        System.out.println("║   ██║     ██║  ██║██║ ╚═╝ ██║███████╗███████╗██║  ██║                 ║");
        System.out.println("║   ╚═╝     ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝                 ║");
        System.out.println("║                                                                       ║");
        System.out.println("║   D I S T R I B U T E D   S Y N C   D E M O                          ║");
        System.out.println("║                                                                       ║");
        System.out.println("║   Testing: STATE_SYNC | CREATE | SET | ADD | REMOVE | REINDEX        ║");
        System.out.println("║                                                                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static SyncOperationListener createOperationListener() {
        return new SyncOperationListener() {
            @Override
            public void onOperationReceived(SyncOperation operation) {
                System.out.println();
                System.out.println("[" + replicaName + "] 📥 RECEIVED: " + operation.getOperationType());
                System.out.println("    Object:   " + operation.getObjectId().substring(0, 8) + "...");
                if (operation.getPropertyIdentifier() != null) {
                    System.out.println("    Property: " + operation.getPropertyIdentifier());
                }
                if (operation.getNewValueSerialized() != null) {
                    System.out.println("    Value:    " + truncate(operation.getNewValueSerialized(), 40));
                }
                System.out.println("    From:     " + operation.getReplicaId().substring(0, 8) + "...");
                System.out.print("[" + replicaName + "] > ");
            }

            @Override
            public void onStateRequested(String requestingReplicaId) {
                System.out.println();
                System.out.println("[" + replicaName + "] 📡 STATE_REQUEST received from: " + 
                        requestingReplicaId.substring(0, 8) + "...");
                System.out.println("    → Sending current state with " + library.getBooks().size() + " books");
                System.out.print("[" + replicaName + "] > ");
            }

            @Override
            public void onStateReceived(String stateSnapshot, String fromReplicaId) {
                System.out.println();
                System.out.println("[" + replicaName + "] 📥 STATE_RESPONSE received from: " + 
                        fromReplicaId.substring(0, 8) + "...");
                System.out.println("    → Restoring state...");
                System.out.print("[" + replicaName + "] > ");
            }

            @Override
            public void onConnected() {
                System.out.println("[" + replicaName + "] 🔗 Connected to sync network");
            }

            @Override
            public void onDisconnected(String reason) {
                System.out.println("[" + replicaName + "] ⚠️ Disconnected: " + reason);
            }

            @Override
            public void onError(Throwable error) {
                System.out.println("[" + replicaName + "] ❌ Error: " + error.getMessage());
            }
        };
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}
