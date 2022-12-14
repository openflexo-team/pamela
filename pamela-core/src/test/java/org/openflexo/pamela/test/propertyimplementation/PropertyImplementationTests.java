package org.openflexo.pamela.test.propertyimplementation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.test.AbstractPAMELATest;

/**
 * Test JML annotations
 * 
 * @author sylvain
 *
 */
public class PropertyImplementationTests extends AbstractPAMELATest {

	private PamelaModelFactory factory;
	private PamelaMetaModel pamelaMetaModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		pamelaMetaModel = new PamelaMetaModel(Concept.class);
		factory = new PamelaModelFactory(pamelaMetaModel);
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
