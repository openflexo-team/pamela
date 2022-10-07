---
sidebar_position: 4
---

# A basic example

*Note that examples (with links to the code) are available by the end of this web page.*

## A very basic model with two entities

The following code listing represents a very basic model with two entities *Book* and *Library*.

Entity *Book* defines two read-write single properties *title* and *ISBN* with single cardinality and with `String` type. Entity *Book* also define a constructor with initial *title* value. Entity *Library* defines a read-write multiple properties *books* referencing *Book* instances.

Note that this code is sufficient to execute the model, while no line of code is required (only java interface and API methods are declared here).

`AccessibleProxyObject` is not mandatory here, but recommanded. This is the interface that PAMELA objects should extend in order to benefit from their default implementation handled by the PAMELA interpreter.

```java
@ModelEntity
interface Book extends AccessibleProxyObject {

  @Initializer
  Book init(@Parameter("title")String aTitle);

  @Getter("title")
  String getTitle();

  @Setter("title")
  void setTitle(String aTitle);

  @Getter("ISBN")
  String getISBN();

  @Setter("ISBN")
  void setISBN(String value);
}

@ModelEntity
interface Library extends AccessibleProxyObject {

  @Getter(value = "books", cardinality = Cardinality.LIST)
  List<Book> getBooks();

  @Adder("books")
  void addToBooks(Book aBook);

  @Remover("books")
  void removeFromBooks(Book aBook);

  @Reindexer("books")
  void moveBookToIndex(Book aBook, int index);

  @Finder(collection = "books", attribute = "title")
  Book getBook(String title);
}
```

Execution of this model may be performed using following simple lines of code.

```java
// Instantiate the meta-model by computing the closure of concepts graph
ModelContext modelContext = ModelContextLibrary.getModelContext(Library.class);

// Instantiate the factory
ModelFactory factory  = new ModelFactory(modelContext);

// Instantiate a Library
Library myLibrary = factory.newInstance(Library.class);

// Instantiate some Books
Book myFirstBook = factory.newInstance(Book.class, "Lord of the ring");
Book anOtherBook = factory.newInstance(Book.class, "Holy bible");
myLibrary.addToBooks(myFirstBook);
myLibrary.addToBooks(anOtherBook);
```

The first line of code instantiates a `ModelContext` (the PAMELA model at runtime) by introspecting and computing the closure of concepts graph obtained while starting from `Library` entity and following `parentEntities` and `properties` relationships. This call builds a *PAMELAModel*, while dynamically following links reflected by compiled byte-code. A factory `ModelFactory` is then instantiated using that `ModelContext`, allowing to instantiate *Library* and *Book* instances.

## Handling custom code

A major challenge to be addressed by MDE (Model-Driven Engineering) approaches is the ability to integrate custom implementation to a base of code derived from a model. The major drawback is the way back (round-tripping).

The PAMELA framework provides an elegant way to do it, while using common extension points such as inheritance, as offered by Java language. Custom implementations should be declared in Java classes, either as a unique implementation (use of `@ImplementationClass`) or with partial implementations (use of multiple  `@Implementation`, see [multiple inheritance and traits programming](./pamela-core/4-multiple_inheritance.md)).

The following example shows how to integrate custom code to the *Book* entity described above. The partial custom implementation is provided by an abstract class, declared in an annotation of its model entity. Custom implementations are defined using usual Java implementation/overrides scheme. Here we define a custom implementation of the `read()` method, which has no annotation (and thus is not processed by the PAMELA framework), and also we customize the getter for *title*, returning a default value when no value is defined for that property. Note that this implementation references default interpreted implementation (call to `performSuperGetter(String)` method).

```java
@ModelEntity
@ImplementationClass(BookImpl.class)
interface Book extends AccessibleProxyObject {

  // ... same as above ...

  void read();
}

// Provides a partial implementation for Book
public static abstract class BookImpl implements Book {

  @Override
  public String getTitle() {
    String title = performSuperGetter("title");
    if (title == null) {
      return "This book has no title";
    }
    return title;
  }

  @Override
    public void read() {
      // do the job
    }
}
```

Notice that the name of a property being a string is fragile, any typo or change of the name may lead to an incorrect code and thus an incorrect model. A best practive is to define the property names as Java constants in the entity interface and use this constant everywhere you need the property name (*e.g.* in the call of `performSuperGetter(String)`).

As said previously, PAMELA framework supports multiple inheritance. In this context, it is possible to provide multiple implementation classes for a given *ModelEntity*. To do so, we use abstract inner classes tagged with `@Implementation`, and the composition is made at run-time (see [multiple inheritance and traits programming](./pamela-core/4-multiple_inheritance.md)).

## Download examples

For each example, we can do :

```
gradle test
```

and

```
gradle run
```

Here are the different versions:

1. [v1.zip](/examples/v1.zip) : minimal example
2. [v2.zip](/examples/v2.zip) : adding behaviour
3. [v3.zip](/examples/v3.zip) : behaviour modifications (AccessibleProxyObject)
4. [v4.zip](/examples/v4.zip) : a more complex example
