package org.openflexo.pamela.test.initializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;

public class TestInitializer {

	/**
	 * Test the factory
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	public void testInitializer() throws ModelDefinitionException {

		ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(A.class, D.class));

		A a = factory.newInstance(A.class, "toto", 42);
		assertEquals("toto", a.getFoo());
		assertEquals((Integer) 42, a.getVal());

		B b = factory.newInstance(B.class, "toto", 42);
		assertEquals("BImpltoto42", b.toString());

		C c = factory.newInstance(C.class, "toto", 42);
		assertEquals("toto", c.getFoo());
		assertEquals((Integer) 84, c.getVal());

		D d = factory.newInstance(D.class, "toto", 42);
		assertEquals("toto", d.getFoo());
		assertEquals((Integer) 252, d.getVal());

	}

}
