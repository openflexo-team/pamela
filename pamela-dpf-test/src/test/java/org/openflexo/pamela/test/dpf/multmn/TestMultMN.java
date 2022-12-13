package org.openflexo.pamela.test.dpf.multmn;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.ppf.PPFViolationException;

public class TestMultMN {

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
		Y y3 = factory.newInstance(Y.class, "y3");

		x1.addToY(y1);
		x2.addToY(y1);
		x2.addToY(y2);
		x2.addToY(y3);

		System.out.println("x1=" + x1);
		System.out.println("x2=" + x2);
		System.out.println("x3=" + x3);

		x1.enableAssertionChecking();
		x2.enableAssertionChecking();
		x3.enableAssertionChecking();

		x1.aMonitoredMethod();

		try {
			x2.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: cardinality (3) is too much
		}

		try {
			x3.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: cardinality (0) is not enough
		}

		x2.removeFromY(y3);
		x3.addToY(y3);

		x1.aMonitoredMethod();
		x2.aMonitoredMethod();
		x3.aMonitoredMethod();

	}

}
