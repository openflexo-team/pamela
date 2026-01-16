/*
 * A book pamela model
 */
package org.openflexo.testPamela.model;

import org.openflexo.pamela.annotations.*;
import org.checkerframework.checker.units.qual.s;
import org.openflexo.pamela.AccessibleProxyObject;

@ModelEntity
@Imports({@Import(org.openflexo.testPamela.model.Novel.class), @Import(org.openflexo.testPamela.model.Journal.class)})
@ImplementationClass(Book.BookImpl.class)
public interface Book extends AccessibleProxyObject {

  static final String TITLE = "title";
  static final String ISBN = "ISBN";
  static final String PAGES = "pages";

  @Initializer
  Book init(@Parameter(TITLE)String aTitle);

  @Getter(TITLE)
  String getTitle();

  @Setter(TITLE)
  void setTitle(String aTitle);

  @Getter(ISBN)
  String getISBN();

  @Setter(ISBN)
  void setISBN(String value);

  @Getter(PAGES)
  Integer getPages();

  @Setter(PAGES)
  void setPages(Integer value);

  boolean isCorrect();

  // Provides a partial implementation for Book
  static abstract class BookImpl implements Book {
    @Override
      public String getISBN() {
        String isbn = (String) performSuperGetter(ISBN);
        if (isbn == null) {
          return "Unknown";
        }
        return isbn;
    }

    @Override
    public Integer getPages() {
      Integer pages = (Integer) performSuperGetter(PAGES);
      if (pages == null) {
        return 100;
      }
      return pages;
    }

    @Override
    public String toString() {
      String title = getTitle();
      String isbn = getISBN();
      Integer pages = getPages();
      return "Book(" + title + "," + isbn + "," + pages + "," + getClass().getSimpleName() + ")";
    }

    public boolean isCorrect() {
        return getTitle() != null && getISBN() != null;
    }
  }
}
