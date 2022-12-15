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
 * Test PAMELA in multiple inheritance context<br>
 * Test the behaviour of Calculator, instance of a multiple inheritance class hierarchy combining multiple partial implemenations
 * 
 * @author sylvain
 * 
 */
public class MultipleInheritanceTest2 {

	/**
	 * Test a factory that should success
	 */
	@Test
	public void testFactory() {

		try {
			PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(Calculator.class));

			ModelEntity<Calculator> calculatorEntity = factory.getMetaModel().getModelEntity(Calculator.class);

			assertNotNull(calculatorEntity);

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test the behaviour of Calculator, instance of a multiple inheritance class hierarchy combining multiple partial implemenations
	 */
	@Test
	public void testInstanciate() throws Exception {

		PamelaModelFactory factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(Calculator.class));

		Calculator calculator = factory.newInstance(Calculator.class);
		assertEquals(-1, calculator.getStoredValue());
		System.out.println("Created calculator with " + calculator.getStoredValue());

		calculator.reset();
		assertEquals(0, calculator.getStoredValue());
		System.out.println("Calculator has now value " + calculator.getStoredValue());

		calculator.processPlus(10);
		assertEquals(10, calculator.getStoredValue());
		System.out.println("Calculator has now value " + calculator.getStoredValue());

		calculator.processPlus(5);
		assertEquals(15, calculator.getStoredValue());
		System.out.println("Calculator has now value " + calculator.getStoredValue());

		calculator.processMinus(8);
		assertEquals(7, calculator.getStoredValue());
		System.out.println("Calculator has now value " + calculator.getStoredValue());

	}
}
