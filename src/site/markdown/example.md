# A basic example

## A very basic model with two entities

The following code listing represents a very basic model with two entities *Book* and *Library*.

Entity *Book* defines two read-write single properties *title* and *ISBN* with single cardinality and with `String` type. Entity *Book* also define a constructor with initial *title* value. Entity *Library* defines a read-write multiple properties *books* referencing *Book* instances.

Note that this code is sufficient to execute the model, while no line of code is required (only java interface and API methods are declared here).

[//]: # (@Sylvain, tu ne parles pas de AccessibleProxyObject)

[//]: # (@Sylvain, les public sont-ils necessaires)


```java
@ModelEntity
public interface Book extends AccessibleProxyObject {

  @Initializer
  public Book init(@Parameter("title")String aTitle);

  @Getter("title")
  public String getTitle();

  @Setter("title")
  public void setTitle(String aTitle);

  @Getter("ISBN")
  public String getISBN();

  @Setter("ISBN")
  public void setISBN(String value);
}

@ModelEntity
public interface Library extends AccessibleProxyObject {

  @Getter(value = "books", cardinality = Cardinality.LIST)
  public List<Book> getBooks();

  @Adder("books")
  public void addToBooks(Book aBook);

  @Remover("books")
  public void removeFromBooks(Book aBook);

  @Reindexer("books")
  public void moveBookToIndex(Book aBook, int index);

  @Finder(collection = "books", attribute = "title")
  public Book getBook(String title);
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
myLibrary.setName("My library");

// Instantiate some Books
Book myFirstBook = factory.newInstance(Book.class, "Lord of the ring");
Book anOtherBook = factory.newInstance(Book.class, "Holy bible");
myLibrary.addToBooks(myFirstBook);
myLibrary.addToBooks(anOtherBook);
```

The first line of code instantiates a `ModelContext` (the PAMELA model at runtime) by introspecting and computing the closure of concepts graph obtained while starting from `Library` entity and following `parentEntities` and `properties` relationships. This call builds a *PAMELAModel*, while dynamically following links reflected by compiled byte-code. A factory `ModelFactory` is then instantiated using that `ModelContext`, allowing to instantiate *Library* and *Book* instances.

## Handling custom code

A major challenge to be addressed by MDE (Model-Driven Engineering) approaches is the ability to integrate custom implementation to a base of code derived from a model. The major drawback is the way back (round-tripping).

The PAMELA framework provides an elegant way to do it, while using common extension points such as inheritance, as offered by Java language. **Custom implementations should be declared in Java classes, this or those classes declared as partial implementation(s) of related *ModelEntity*.**

The following example shows how to integrate custom code to the **fully interpreted** *Book* entity described above. The partial custom implementation is provided by an abstract class, declared in an annotation of its model entity. Custom implementations are defined using usual Java implementation/overrides scheme. Here we define a custom implementation of the `read()` method, which has no annotation (and thus is not processed by the PAMELA framework), and also we customize the getter for *title*, returning a default value when no value is defined for that property. Note that this implementation references default interpreted implementation (call to `performSuperGetter(String)` method).

```java
@ModelEntity
@ImplementationClass(BookImpl.class)
public interface Book extends AccessibleProxyObject {

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

Notice that the name of a property being a string is fragile, any typo or change of the name may lead to an incorrect code and thus an incorrect model. A best practive is to define the property names as Java constants in the entity interface and use this constant everywhere you need the property name (*e.g.* in the call of `performSuperGetter`).

As said previously, PAMELA framework supports multiple inheritance. In this context, it is possible to provide multiple implementation classes for a given *ModelEntity*. To do so, we use abstract inner classes tagged with `@Implementation`, and the composition is made at run-time.
[//]: # (@Sylvain, pas vraiment compris, un exemple de code ?)


[<< Approach overview](./overview.html) \| [Common annotations >>](./annotations.html)
