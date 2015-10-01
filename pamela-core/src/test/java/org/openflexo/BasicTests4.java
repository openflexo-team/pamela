package org.openflexo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model5.MyContainer;
import org.openflexo.model5.MyContents;
import org.openflexo.model5.MySpecializedContainer;
import org.openflexo.model5.MySpecializedContents;

/**
 * Test PAMELA in overriden getter context
 * 
 * @author sylvain
 * 
 */
public class BasicTests4 {

	/**
	 * Test the factory
	 */
	@Test
	public void testFactory() {

		try {
			ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(MySpecializedContainer.class,
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

		ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(MySpecializedContainer.class,
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
