package org.openflexo.pamela.test.dpf.nonoverlapping;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.annotations.MonitoredEntity.MonitoringStrategy;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.ppf.PPFViolationException;
import org.openflexo.pamela.test.dpf.AbstractConcept;

public class TestNonOverlapping {

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

		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(X.class));
		ModelEntity<AbstractConcept> abstractConceptEntity = factory.getMetaModel().getModelEntity(AbstractConcept.class);
		abstractConceptEntity.setMonitoringStrategy(monitoringStrategy);

		X x1 = factory.newInstance(X.class, "x1");
		X x2 = factory.newInstance(X.class, "x2");

		Y y1 = factory.newInstance(Y.class, "y1");
		Y y2 = factory.newInstance(Y.class, "y2");
		Y y3 = factory.newInstance(Y.class, "y3");

		x1.addToY(y1);
		x1.addToY(y2);
		x2.addToY(y1);
		x2.addToY(y3);

		x1.enableAssertionChecking();
		x2.enableAssertionChecking();

		try {
			x1.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: y1 is present in both x1 and x2 images trough y relation
		}

		try {
			x2.aMonitoredMethod();
			fail();
		} catch (PPFViolationException e) {
			// Invariant violation: y1 is present in both x1 and x2 images trough y relation
		}

		if (monitoringStrategy == MonitoringStrategy.CheckMonitoredMethodsOnly) {
			x1.removeFromY(y1);
			x1.aMonitoredMethod();
			x2.aMonitoredMethod();
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

			x1.removeFromY(y1);

			x1.enableAssertionChecking();
			x2.enableAssertionChecking();

			x1.aMonitoredMethod();
			x2.aMonitoredMethod();

		}

	}

}
