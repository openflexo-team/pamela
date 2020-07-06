package org.flexo.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.model6.MyContainer;
import org.openflexo.model6.MyContainerImpl;
import org.openflexo.model6.MyContents;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test PAMELA in overriden getter/setter context
 * 
 * @author xtof
 * 
 */
public class BasicTests5 {

	/**
	 * Test the factory
	 */
	@Test
	public void testFactory() {

		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(MyContainer.class, MyContents.class));

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

		ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(MyContainer.class, MyContents.class));

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

		System.out.println("Et au finale je dis:" + container1.getContents());
		System.out.println("Et au finale je dis:" + container2.getContents());

	}

}
