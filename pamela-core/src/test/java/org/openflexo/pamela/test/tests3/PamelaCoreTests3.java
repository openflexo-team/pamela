package org.openflexo.pamela.test.tests3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test PAMELA in overriden getter context
 * 
 * @author sylvain
 * 
 */
public class PamelaCoreTests3 {

	/**
	 * Test the factory
	 */
	@Test
	public void testFactory() {

		try {
			PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(MySpecializedContainer.class,
					MySpecializedContents.class));

			ModelEntity<MyContainer> myContainerEntity = factory.getModelContext().getModelEntity(MyContainer.class);
			ModelEntity<MySpecializedContainer> mySpecializedContainerEntity = factory.getModelContext().getModelEntity(
					MySpecializedContainer.class);
			ModelEntity<MyContents> myContentsEntity = factory.getModelContext().getModelEntity(MyContents.class);
			ModelEntity<MySpecializedContents> mySpecializedContentsEntity = factory.getModelContext().getModelEntity(
					MySpecializedContents.class);

			assertNotNull(myContainerEntity);
			assertNotNull(mySpecializedContainerEntity);
			assertNotNull(myContentsEntity);
			assertNotNull(mySpecializedContentsEntity);

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

		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(MySpecializedContainer.class,
				MySpecializedContents.class));

		MySpecializedContainer container = factory.newInstance(MySpecializedContainer.class);
		MySpecializedContents contents = factory.newInstance(MySpecializedContents.class);

		try {
			container.setContents(contents);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertEquals(contents, container.getContents());

	}

}
