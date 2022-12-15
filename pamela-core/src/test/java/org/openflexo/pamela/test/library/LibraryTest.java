package org.openflexo.pamela.test.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.validation.DefaultValidationModel;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;

/**
 * Test PAMELA on Library-Book model
 * 
 * @author sylvain
 * 
 */
public class LibraryTest {

	/**
	 * Instantiate factory
	 */
	@Test
	public void testFactory() {

		try {
			PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(Library.class));

			ModelEntity<Library> libraryEntity = factory.getMetaModel().getModelEntity(Library.class);
			ModelEntity<Book> bookEntity = factory.getMetaModel().getModelEntity(Book.class);

			assertNotNull(libraryEntity);
			assertNotNull(bookEntity);

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Instantiate a Library with two books
	 */
	@Test
	public void testInstanciate() throws Exception {

		// Instantiate the meta-model
		// by computing the closure of concepts graph
		PamelaMetaModel pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(Library.class);
		// Instantiate the factory
		PamelaModelFactory factory = new PamelaModelFactory(pamelaMetaModel);
		// Instantiate a Library
		Library myLibrary = factory.newInstance(Library.class);
		myLibrary.setName("My library");
		// Instantiate some Books
		Book myFirstBook = factory.newInstance(Book.class, "Lord of the ring");
		Book anOtherBook = factory.newInstance(Book.class, "Holy bible");
		myLibrary.addToBooks(myFirstBook);
		myLibrary.addToBooks(anOtherBook);

		ValidationModel validationModel = new DefaultValidationModel(pamelaMetaModel);
		ValidationReport validationReport = new ValidationReport(validationModel, myLibrary);
		System.out.println(validationReport.reportAsString());

		assertEquals(2, validationReport.getErrorsCount());
		// VALIDATION / ERROR: Book does not define ISBN code
		// VALIDATION / ERROR: Book does not define ISBN code

	}

}
