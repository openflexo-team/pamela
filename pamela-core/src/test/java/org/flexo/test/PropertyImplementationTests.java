package org.flexo.test;

import org.flexo.model9.Concept;
import org.flexo.model9.SubConcept;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * Test JML annotations
 * 
 * @author sylvain
 *
 */
public class PropertyImplementationTests extends AbstractPAMELATest {

	private ModelFactory factory;
	private ModelContext modelContext;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		modelContext = new ModelContext(Concept.class);
		factory = new ModelFactory(modelContext);
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNormalBehaviour() throws Exception {
		Concept concept = factory.newInstance(Concept.class);
		concept.setValue("Foo");
		concept.setValue("Toto");

		System.out.println("hop: " + concept.getValue());

		SubConcept subConcept1 = factory.newInstance(SubConcept.class);
		subConcept1.setName("subConcept1");
		SubConcept subConcept2 = factory.newInstance(SubConcept.class);
		subConcept2.setName("subConcept2");
		SubConcept subConcept3 = factory.newInstance(SubConcept.class);
		subConcept3.setName("subConcept3");

		concept.addToSubConcepts(subConcept1);
		concept.addToSubConcepts(subConcept2);
		concept.addToSubConcepts(subConcept3);

		System.out.println(factory.stringRepresentation(concept));

		System.out.println("main concept for subConcept1 : " + subConcept1.getMainConcept());
	}

}
