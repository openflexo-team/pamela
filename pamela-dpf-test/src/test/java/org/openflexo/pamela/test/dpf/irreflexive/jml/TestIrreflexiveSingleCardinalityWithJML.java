package org.openflexo.pamela.test.dpf.irreflexive.jml;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.annotations.MonitoredEntity.MonitoringStrategy;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.jml.JMLAssertionViolationException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.test.dpf.AbstractConcept;

public class TestIrreflexiveSingleCardinalityWithJML {

	@Test
	public void testCheckMonitoredMethodsOnly() throws ModelDefinitionException {
		performTest(MonitoringStrategy.CheckMonitoredMethodsOnly);
	}

	@Test
	public void testCheckInterpretedAndMonitoredMethods() throws ModelDefinitionException {
		performTest(MonitoringStrategy.CheckInterpretedAndMonitoredMethods);
	}

	@Test
	public void testCheckAllMethodsExcludeUnmonitored() throws ModelDefinitionException {
		performTest(MonitoringStrategy.CheckAllMethodsExcludeUnmonitored);
	}

	@Test
	public void testCheckAllMethods() throws ModelDefinitionException {
		performTest(MonitoringStrategy.CheckAllMethods);
	}

	private void performTest(MonitoringStrategy monitoringStrategy) throws ModelDefinitionException {

		ModelFactory factory = new ModelFactory(ModelContextLibrary.getModelContext(X.class));
		ModelEntity<AbstractConcept> abstractConceptEntity = factory.getModelContext().getModelEntity(AbstractConcept.class);
		abstractConceptEntity.setMonitoringStrategy(monitoringStrategy);

		ModelEntity<X> xEntity = factory.getModelContext().getModelEntity(X.class);
		System.out.println("MonitoringStategy: " + xEntity.getMonitoringStrategy());

		X x1 = factory.newInstance(X.class, "x1");
		X x2 = factory.newInstance(X.class, "x2");

		x1.setSingleX(x1);

		x1.enableAssertionChecking();
		x2.enableAssertionChecking();

		try {
			x1.aMonitoredMethod();
			fail();
		} catch (JMLAssertionViolationException e) {
			// Invariant violation: getSingleY() == null
		}

		if (monitoringStrategy == MonitoringStrategy.CheckMonitoredMethodsOnly) {
			x1.setSingleX(x2);
			x1.aMonitoredMethod();
		}
		else {
			// Call to x1.setSingleX(x2) will trigger property checking which will fail
			try {
				x1.setSingleX(x2);
				x1.aMonitoredMethod();
			} catch (JMLAssertionViolationException e) {
				// Invariant violation
			}

			// Disable checking to "repair" property
			x1.disableAssertionChecking();
			x1.setSingleX(x2);
			x1.enableAssertionChecking();
			x1.aMonitoredMethod();

		}

	}

}
