package org.openflexo.pamela.test.dpf.total.jml;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.annotations.MonitoredEntity.MonitoringStrategy;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.jml.JMLAssertionViolationException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.test.dpf.AbstractConcept;

public class TestTotalMultipleCardinalityWithJML {

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

		x1.addToMultipleY(y1);
		x2.addToMultipleY(y1);
		x2.addToMultipleY(y2);

		System.out.println("x1=" + x1);
		System.out.println("x2=" + x2);
		System.out.println("x3=" + x3);

		x1.enableAssertionChecking();
		x2.enableAssertionChecking();
		x3.enableAssertionChecking();

		try {
			x3.aMonitoredMethod();
			fail();
		} catch (JMLAssertionViolationException e) {
			// Invariant violation: object.singleY != null as expected
		}

		if (monitoringStrategy == MonitoringStrategy.CheckMonitoredMethodsOnly) {
			Y y3 = factory.newInstance(Y.class, "y3");
			x3.addToMultipleY(y3);
			x3.aMonitoredMethod();
		}
		else {
			// Call to x3.setSingleY(y3) will trigger property checking which will fail
			try {
				Y y3 = factory.newInstance(Y.class, "y3");
				x3.addToMultipleY(y3);
				x3.aMonitoredMethod();
			} catch (JMLAssertionViolationException e) {
				// Invariant violation
			}
		}

	}

}
