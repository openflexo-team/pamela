package org.openflexo.pamela.test.library;

import java.util.List;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Reindexer;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.validation.Validable;

@ModelEntity
public interface Library extends AccessibleProxyObject, Validable {

	@Getter("name")
	public String getName();

	@Setter("name")
	public void setName(String aName);

	@Getter(value = "books", cardinality = Cardinality.LIST)
	@Embedded
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
