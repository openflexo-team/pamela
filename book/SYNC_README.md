# PAMELA Distributed Synchronization Demo

Ce dossier contient un exemple de synchronisation distribuée d'instances PAMELA 
utilisant RabbitMQ comme broker de messages.

## Architecture

```
┌─────────────────┐     RabbitMQ      ┌─────────────────┐
│    ReplicaA     │◄──────────────────►│    ReplicaB     │
│   (Terminal 1)  │   fanout exchange │   (Terminal 2)  │
│                 │  pamela-library-  │                 │
│  ┌───────────┐  │       sync        │  ┌───────────┐  │
│  │  Library  │  │                   │  │  Library  │  │
│  │  ┌─────┐  │  │                   │  │  ┌─────┐  │  │
│  │  │Book1│  │  │ ◄── ADD Book ──►  │  │  │Book1│  │  │
│  │  │Book2│  │  │ ◄── SET prop ──►  │  │  │Book2│  │  │
│  │  └─────┘  │  │ ◄── REMOVE ────►  │  │  └─────┘  │  │
│  └───────────┘  │                   │  └───────────┘  │
└─────────────────┘                   └─────────────────┘
```

## Prérequis

1. **Java 17+** installé
2. **Docker** installé et lancé
3. **Gradle** (ou utiliser le wrapper `gradlew`)

## Étapes de Test

### 1. Démarrer RabbitMQ (Docker)

```bash
docker run -d --name rabbitmq \
    -p 5672:5672 \
    -p 15672:15672 \
    rabbitmq:3-management
```

Accéder à l'interface d'administration : http://localhost:15672  
Identifiants par défaut : `guest` / `guest`

### 2. Compiler le projet

```bash
cd book
.\gradlew build
```

### 3. Terminal 1 - Lancer ReplicaA

```bash
.\gradlew run -PmainClass=org.openflexo.testPamela.ReplicaA
```

Ou avec Java directement :
```bash
java -cp build/libs/book.jar:../pamela-core/build/libs/* org.openflexo.testPamela.ReplicaA
```

Notez l'**ID de la Library** affiché (ex: `a3b4c5d6-...`)

### 4. Terminal 2 - Lancer ReplicaB

```bash
.\gradlew run -PmainClass=org.openflexo.testPamela.ReplicaB
```

Quand demandé, entrez l'ID de la Library affiché par ReplicaA.

### 5. Tester la synchronisation

**Dans Terminal 1 (ReplicaA) :**
```
[A] > add Harry Potter
[A] ✓ Added: Book(title=Harry Potter)
```

**Dans Terminal 2 (ReplicaB) :** (mise à jour automatique)
```
[B] 📥 Received: ADD on books = Book(title=Harry Potter)
```

**Dans Terminal 2 (ReplicaB) :**
```
[B] > add Le Seigneur des Anneaux
```

**Dans Terminal 1 (ReplicaA) :** (mise à jour automatique)
```
[A] 📥 Received: ADD on books = ...
```

## Commandes disponibles

| Commande | Description |
|----------|-------------|
| `add <titre>` | Ajoute un livre à la bibliothèque |
| `remove <titre>` | Supprime un livre par son titre |
| `list` | Affiche tous les livres |
| `quit` | Quitte l'application |

## Structure des fichiers

```
book/
├── build.gradle              # Dépendances (RabbitMQ, Jackson)
├── SYNC_README.md           # Ce fichier
└── src/main/java/org/openflexo/testPamela/
    ├── model/
    │   ├── Book.java        # Modèle PAMELA d'un livre
    │   ├── Library.java     # Modèle PAMELA d'une bibliothèque
    │   ├── Novel.java       # Sous-type de Book
    │   └── Journal.java     # Sous-type de Book
    ├── App.java             # Exemple original simple
    ├── ReplicaA.java        # Première instance collaborative
    └── ReplicaB.java        # Seconde instance collaborative
```

## Comment ça marche ?

1. **SyncEditingContext** : Contexte d'édition étendu qui intercepte les modifications
2. **RabbitMQSyncManager** : Gère la connexion à RabbitMQ et la diffusion des messages
3. **ObjectIdentityManager** : Associe chaque objet PAMELA à un UUID unique
4. **SyncOperation** : Représente une opération (CREATE, SET, ADD, REMOVE, DELETE)
5. **VectorClock** : Horloge vectorielle pour l'ordre causal des opérations

Quand vous faites `library.addToBooks(book)` :
1. Le `ProxyMethodHandler` détecte l'appel au setter
2. Il broadcast une `SyncOperation` via `SyncEditingContext`
3. Le `RabbitMQSyncManager` publie le message JSON sur l'exchange
4. Toutes les réplicas reçoivent le message et appliquent l'opération

## Dépannage

### RabbitMQ ne démarre pas
```bash
docker logs rabbitmq
```

### Erreur de connexion
Vérifiez que le port 5672 est accessible :
```bash
docker ps | findstr rabbitmq
```

### Les messages ne se synchronisent pas
1. Vérifiez que les deux réplicas utilisent le même Library ID
2. Consultez les messages dans RabbitMQ Management UI (http://localhost:15672)
3. Vérifiez l'exchange "pamela-library-sync"

## Arrêter RabbitMQ

```bash
docker stop rabbitmq
docker rm rabbitmq
```
