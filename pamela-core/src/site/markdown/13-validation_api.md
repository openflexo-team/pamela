# Validation API

PAMELA framework natively offers a validation definition scheme at *ModelEntity* level. To be validable, the *ModelEntity* support interface must extends `Validable` interface. This allows to define a set of `ValidationRules`.

Following excerpt of code shows how a `ValidationRule` should be declared on a *ModelEntity*, exposed as a inner static class annotated with `@DefineValidationRule` annotation. In this example, an instance of `Book` should have an ISBN code. Note that relatively to Java semantics, any instance of `Book` may have `null` ISBN code. Only the validation level raises a validation rule violation.

```java
@ModelEntity
public interface Book extends AccessibleProxyObject, Validable {
    ...

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
```

Validation performing API is presented on following excerpt of code. A `ValidationReport` is generated as result artefact for the validation of a `Validable` object, given the `ValidationModel` directely computed from `ModelContext`.

```java
// Instantiate the model
ModelContext modelContext = ModelContextLibrary.getModelContext(Library.class);
... 
// instantiate here the factory and myLibrary object
ValidationModel validationModel = new DefaultValidationModel(modelContext);
ValidationReport validationReport = new ValidationReport(validationModel, myLibrary);

assertEquals(2, validationReport.getErrorsCount());
// myLibrary object contains here two books without ISBN code
// VALIDATION / ERROR: Book does not define ISBN code
// VALIDATION / ERROR: Book does not define ISBN code
```


 

    
  
