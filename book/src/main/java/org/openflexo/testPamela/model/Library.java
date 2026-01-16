/*
 * A book pamela model
 */
package org.openflexo.testPamela.model;

import org.openflexo.pamela.annotations.*;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.AccessibleProxyObject;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.List;

@ModelEntity
@ImplementationClass(Library.LibraryImpl.class)
public interface Library extends AccessibleProxyObject, Iterable<Book> {

  static String BOOKS = "books";
  static String FILE = "file";

  @Getter(value = BOOKS, cardinality = Cardinality.LIST)
  List<Book> getBooks();

  @Getter(value = FILE, ignoreType = true)
  FileFilter getFile();

  @Setter(FILE)
  void setFile(FileFilter file);

  @Adder(BOOKS)
  void addToBooks(Book aBook);

  @Remover(BOOKS)
  void removeFromBooks(Book aBook);

  @Reindexer(BOOKS)
  void moveBookToIndex(Book aBook, int index);

  @Finder(collection = BOOKS, attribute = "title")
  Book getBook(String title);

  // Provides a partial implementation for Book
  static abstract class LibraryImpl implements Library {
    @Override
    public String toString() {
      StringBuilder str = new StringBuilder("[");
      boolean first =true;
      for (Book b : getBooks()) {
        if (!first) {
          str.append(",");
        }
        str.append(b);
      }
      str.append("]");
      return str.toString();
    }
    
    @Override
    public Iterator<Book> iterator() {
    	return getBooks().iterator();
    }
  }
}
