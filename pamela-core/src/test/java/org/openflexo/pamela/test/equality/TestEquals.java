package org.openflexo.pamela.test.equality;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;

public class TestEquals {

	/**
	 * Test the factory
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	public void testEquals() throws ModelDefinitionException {

		ModelFactory factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(A.class, B.class));

		A a1 = factory.newInstance(A.class);
		B b1 = factory.newInstance(B.class);
		a1.setFoo("foo");
		a1.setB(b1);
		b1.setFoo2("foo2");
		b1.setA(a1);

		A a2 = factory.newInstance(A.class);
		B b2 = factory.newInstance(B.class);
		a2.setFoo("foo");
		a2.setB(b2);
		b2.setFoo2("foo2");
		b2.setA(a2);

		assertTrue(a1.equalsObject(a2));

	}

}
