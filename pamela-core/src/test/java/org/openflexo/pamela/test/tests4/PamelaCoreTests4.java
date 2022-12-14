package org.openflexo.pamela.test.tests4;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test PAMELA in overriden getter/setter context
 * 
 * @author xtof
 * 
 */
public class PamelaCoreTests4 {

	/**
	 * Test the factory
	 */
	@Test
	public void testFactory() {

		try {
			PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.getCompoundModelContext(MyContainer.class, MyContents.class));

			ModelEntity<MyContainer> myContainerEntity = factory.getModelContext().getModelEntity(MyContainer.class);
			ModelEntity<MyContents> myContentsEntity = factory.getModelContext().getModelEntity(MyContents.class);

			assertNotNull(myContainerEntity);
			assertNotNull(myContentsEntity);

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test the diagram factory
	 */
	@Test
	public void testInstanciate() throws Exception {

		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.getCompoundModelContext(MyContainer.class, MyContents.class));

		MyContainer container1 = factory.newInstance(MyContainer.class);
		MyContainer container2 = factory.newInstance(MyContainer.class);
		((MyContainerImpl) container1).setFactory(factory);
		((MyContainerImpl) container2).setFactory(factory);

		try {
			container1.setContents("Bonjour");

			container1.setContents("Au revoir");

			container1.setContents("A demain");

			container2.setContentURI("Content://Je suis méchant");

		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// TODO : this should be assertTrue!!
		assertTrue(container1.getContents().equals("A demain"));

		assertTrue(container2.getContents().equals("Je suis méchant"));

		System.out.println("Et au final je dis:" + container1.getContents());
		System.out.println("Et au final je dis:" + container2.getContents());

	}

}
