package org.openflexo.pamela.test.tests2;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.test.AbstractPAMELATest;

/**
 * Basic tests regarding a sample PAMELA model
 * 
 * @author sylvain
 *
 */
public class PamelaCoreTests2 extends AbstractPAMELATest {

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
		pamelaMetaModel = new PamelaMetaModel(PAMFlexoResource.class);
		factory = new PamelaModelFactory(pamelaMetaModel);
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testResources() throws Exception {
		PAMFlexoResource<?> container = factory.newInstance(PAMFlexoResource.class);
		PAMFlexoResource<?> content1 = factory.newInstance(PAMFlexoResource.class);
		PAMFlexoResource<?> content2 = factory.newInstance(PAMFlexoResource.class);
		container.addToContents(content1);
		content2.setContainer(container);
		assertEquals(container, content1.getContainer());
		assertEquals(container, content2.getContainer());
		assertTrue(container.getContents().contains(content1));
		assertTrue(container.getContents().contains(content2));
	}
}
