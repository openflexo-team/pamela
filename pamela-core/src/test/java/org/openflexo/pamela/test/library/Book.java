package org.openflexo.pamela.test.library;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

@ModelEntity
public interface Book extends AccessibleProxyObject, Validable {

	@Initializer
	public Book init(@Parameter("title") String aTitle);

	@Getter("title")
	public String getTitle();

	@Setter("title")
	public void setTitle(String aTitle);

	@Getter("ISBN")
	public String getISBN();

	@Setter("ISBN")
	public void setISBN(String value);

	@DefineValidationRule
	public static class BookShouldHaveAnISBN extends ValidationRule<BookShouldHaveAnISBN, Book> {
		public BookShouldHaveAnISBN() {
			super(Book.class, "Book should have an ISBN code");
		}

		@Override
		public ValidationIssue<BookShouldHaveAnISBN, Book> applyValidation(Book book) {
			if (book.getISBN() == null) {
				return new ValidationError<>(this, book, "Book does not define ISBN code");
			}
			return null;
		}
	}

}
