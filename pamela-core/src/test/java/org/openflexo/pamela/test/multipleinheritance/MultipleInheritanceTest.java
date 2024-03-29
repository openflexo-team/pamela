package org.openflexo.pamela.test.multipleinheritance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Test PAMELA in multiple inheritance context
 * 
 * @author sylvain
 * 
 */
public class MultipleInheritanceTest {

	/**
	 * Test a factory that should fail on ModelDefinitionException: Multiple inheritance implementation clash
	 */
	@Test
	public void testFactory() {

		try {
			// Var unused PamelaModelFactory factory =
			new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(A.class, B.class, C.class, D1.class));

			fail();
		} catch (ModelDefinitionException e) {
			System.out.println("Failed as expected");
		}
	}

	/**
	 * Test a factory that should success
	 */
	@Test
	public void testFactory2() {

		try {
			PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(A.class, B.class, C.class, D2.class));

			ModelEntity<A> AEntity = factory.getModelContext().getModelEntity(A.class);
			ModelEntity<B> BEntity = factory.getModelContext().getModelEntity(B.class);
			ModelEntity<C> CEntity = factory.getModelContext().getModelEntity(C.class);
			ModelEntity<D2> D2Entity = factory.getModelContext().getModelEntity(D2.class);

			assertNotNull(AEntity);
			assertNotNull(BEntity);
			assertNotNull(CEntity);
			assertNotNull(D2Entity);

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

		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(A.class, B.class, C.class, D2.class));

		D2 d2 = factory.newInstance(D2.class);

		System.out.println("Foo=" + d2.getFoo());
		System.out.println("Foo2=" + d2.getFoo2());

		assertEquals("foo in class D2", d2.getFoo());
		assertEquals("foo2 in class C, foo=foo in class D2", d2.getFoo2());

		d2.methodExecution();

	}
}
