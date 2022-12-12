package org.openflexo.pamela.test.dpf.total.jml;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.jml.SpecificationsViolationException;

public class TestTotalSingleCardinality {

	/**
	 * Test the factory
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	public void testInitializer() throws ModelDefinitionException {

		ModelFactory factory = new ModelFactory(ModelContextLibrary.getModelContext(X.class));

		X x1 = factory.newInstance(X.class, "x1");
		X x2 = factory.newInstance(X.class, "x2");
		X x3 = factory.newInstance(X.class, "x3");

		Y y1 = factory.newInstance(Y.class, "y1");
		Y y2 = factory.newInstance(Y.class, "y2");

		x1.setSingleY(y1);
		x2.setSingleY(y2);

		System.out.println("x1=" + x1);
		System.out.println("x2=" + x2);
		System.out.println("x3=" + x3);

		x1.enableAssertionChecking();
		x2.enableAssertionChecking();
		x3.enableAssertionChecking();

		try {
			x3.aMonitoredMethod();
			fail();
		} catch (SpecificationsViolationException e) {
			// Invariant violation: object.singleY != null as expected
		}

		Y y3 = factory.newInstance(Y.class, "y3");
		x3.setSingleY(y3);

		x3.aMonitoredMethod();

	}

}
