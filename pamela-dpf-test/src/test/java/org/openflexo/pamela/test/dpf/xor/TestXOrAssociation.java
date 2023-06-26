package org.openflexo.pamela.test.dpf.xor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.annotations.MonitoredEntity.MonitoringStrategy;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.ppf.patterns.xor.XOrAssociationDefinition;
import org.openflexo.pamela.test.dpf.AbstractConcept;

// TODO: test with multiple cardinalities
public class TestXOrAssociation {

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

		PamelaMetaModel metaModel = PamelaMetaModelLibrary.retrieveMetaModel(X.class);
		assertEquals(1, metaModel.getPatternDefinitions(XOrAssociationDefinition.class).size());
		XOrAssociationDefinition xOrDefinition = metaModel.getPatternDefinitions(XOrAssociationDefinition.class).get(0);

		PamelaModelFactory factory = new PamelaModelFactory(metaModel);
		ModelEntity<AbstractConcept> abstractConceptEntity = factory.getMetaModel().getModelEntity(AbstractConcept.class);
		abstractConceptEntity.setMonitoringStrategy(monitoringStrategy);

		X x1 = factory.newInstance(X.class, "x1");
		X x2 = factory.newInstance(X.class, "x2");

		Y y = factory.newInstance(Y.class, "y");

		Z z = factory.newInstance(Z.class, "z");

		x1.enableAssertionChecking();
		x1.setY(y);
		x1.aMonitoredMethod();

		x2.enableAssertionChecking();
		try {
			x2.aMonitoredMethod();
			fail();
		} catch (ModelExecutionException e) {
			// Assertion violation : both values are null
		}

		x2.disableAssertionChecking();
		x2.setY(y);
		x2.setZ(z);
		x2.enableAssertionChecking();
		try {
			x2.aMonitoredMethod();
			fail();
		} catch (ModelExecutionException e) {
			// Assertion violation : both values are null
		}
		x2.setZ(null);

	}

}
