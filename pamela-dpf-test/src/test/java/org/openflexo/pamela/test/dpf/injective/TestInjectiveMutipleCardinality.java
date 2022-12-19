package org.openflexo.pamela.test.dpf.injective;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.annotations.MonitoredEntity.MonitoringStrategy;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.ppf.PPFViolationException;
import org.openflexo.pamela.test.dpf.AbstractConcept;

public class TestInjectiveMutipleCardinality {

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

		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(XMultipleY.class));
		ModelEntity<AbstractConcept> abstractConceptEntity = factory.getMetaModel().getModelEntity(AbstractConcept.class);
		abstractConceptEntity.setMonitoringStrategy(monitoringStrategy);

		XMultipleY x1 = factory.newInstance(XMultipleY.class, "x1");
		XMultipleY x2 = factory.newInstance(XMultipleY.class, "x2");
		XMultipleY x3 = factory.newInstance(XMultipleY.class, "x3");

		Y y1 = factory.newInstance(Y.class, "y1");
		Y y2 = factory.newInstance(Y.class, "y2");
		Y y3 = factory.newInstance(Y.class, "y3");

		x1.addToY(y1);
		x1.addToY(y2);
		x2.addToY(y1);
		x2.addToY(y2);
		x3.addToY(y1);
		x3.addToY(y2);

		x1.enableAssertionChecking();
		x2.enableAssertionChecking();
		x3.enableAssertionChecking();

		try {
			x1.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: same value (y1,y2) for x1 x2 and x3
		}

		try {
			x2.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: same value (y1,y2) for x1 x2 and x3
		}

		try {
			x3.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: same value (y1,y2) for x1 x2 and x3
		}

		if (monitoringStrategy == MonitoringStrategy.CheckMonitoredMethodsOnly) {
			x1.removeFromY(y1);
			try {
				x1.aMonitoredMethod();
			} catch (PPFViolationException e) {
				// Invariant violation: still same value (y1,y2) for x2 and x3
			}
			x3.addToY(y3);
			x1.aMonitoredMethod();
			x2.aMonitoredMethod();
			x3.aMonitoredMethod();
		}
		else {
			// Call to x1.removeFromY(y1) will trigger property checking which will fail
			try {
				x1.removeFromY(y1);
			} catch (PPFViolationException e) {
				// Invariant violation
			}

			x1.disableAssertionChecking();
			x2.disableAssertionChecking();
			x3.disableAssertionChecking();

			x1.removeFromY(y1);
			x3.addToY(y3);

			x1.enableAssertionChecking();
			x2.enableAssertionChecking();
			x3.enableAssertionChecking();

			x1.aMonitoredMethod();
			x2.aMonitoredMethod();
			x3.aMonitoredMethod();

		}

	}

}
