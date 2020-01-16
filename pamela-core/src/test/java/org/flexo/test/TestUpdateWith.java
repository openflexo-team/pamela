package org.flexo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflexo.model8.ConceptA;
import org.openflexo.model8.ConceptB;
import org.openflexo.model8.ConceptC;
import org.openflexo.model8.ConceptC1;
import org.openflexo.model8.ConceptC2;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * Test PAMELA updateWith(Object) feature
 * 
 * @author sylvain
 * 
 */
public class TestUpdateWith {

	private static ModelFactory createFactory() {
		try {
			return new ModelFactory(ModelContextLibrary.getCompoundModelContext(ConceptA.class, ConceptC1.class, ConceptC2.class));
		} catch (ModelDefinitionException e) {
			return null;
		}

	}

	private static class TestChangeListener implements PropertyChangeListener {
		List<PropertyChangeEvent> events = new ArrayList<>();

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("serializing")) {
				return;
			}
			events.add(evt);
		}
	}

	private static boolean isEqual(Object oldValue, Object newValue) {
		if (oldValue == null) {
			return newValue == null;
		}
		if (oldValue == newValue) {
			return true;
		}
		return oldValue.equals(newValue);

	}

	private static void assertPropertyNotified(Object source, String propertyName, Object oldValue, Object newValue,
			TestChangeListener listener) {
		for (PropertyChangeEvent evt : listener.events) {
			if (evt.getSource().equals(source) && evt.getPropertyName().equals(propertyName) && isEqual(evt.getOldValue(), oldValue)
					&& isEqual(evt.getNewValue(), newValue)) {
				return;
			}
		}
		fail("Property notification " + propertyName + " from " + oldValue + " to " + newValue + " not fired !");
	}

	private static double getDistance(ModelFactory factory, Object o1, Object o2) {
		return factory.getHandler(o1).getDistance(o2);
	}

	@Test
	public void testSimpleUpdating() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptA a2 = factory.newInstance(ConceptA.class);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare

		// Same object !
		a1.updateWith(a2);
		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare
		assertEquals(0, listener.events.size());

		// Test update the value of ConceptA

		a1.setValue("Une valeur");

		a2.setValue("Une valeur");
		assertTrue(a1.equalsObject(a2));
		assertEquals(0.0, getDistance(factory, a1, a2), 0.01);

		a2.setValue("Une valeur2");
		assertFalse(a1.equalsObject(a2));
		assertEquals(0.09, getDistance(factory, a1, a2), 0.01);

		a2.setValue("Une autre valeur");
		assertFalse(a1.equalsObject(a2));
		assertEquals(0.375, getDistance(factory, a1, a2), 0.01);

		TestChangeListener updateListener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(updateListener);
		a1.updateWith(a2);

		assertEquals("Une autre valeur", a1.getValue());

		assertEquals(1, updateListener.events.size());
		assertPropertyNotified(a1, "value", "Une valeur", "Une autre valeur", listener);
	}

	@Test
	public void testUpdatingWithEmbedding() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		assertTrue(a1.equalsObject(a2));

		a2.setValue("Coucou");
		assertFalse(a1.equalsObject(a2));
		assertEquals(0.277, getDistance(factory, a1, a2), 0.01);

		assertTrue(b1.equalsObject(b2));
		assertEquals(0.0, getDistance(factory, b1, b2), 0.01);

		b2.setU1("Bijour");
		b2.setU2("Le monde !!!");

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertFalse(b1.equalsObject(b2));
		assertEquals(0.226, getDistance(factory, b1, b2), 0.01);

		assertFalse(a1.equalsObject(a2));
		assertEquals(0.428, getDistance(factory, a1, a2), 0.01);
		// System.out.println("d1: " + factory.getHandler(a1).getDistanceBetweenValues(a1.getValue(), a2.getValue()));
		// System.out.println("d2: " + factory.getHandler(a1).getDistanceBetweenValues(a1.getConceptB(), a2.getConceptB()));

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals("Coucou", a1.getValue());
		assertEquals("Bijour", b1.getU1());
		assertEquals("Le monde !!!", b1.getU2());

		assertEquals(3, listener.events.size());

		assertPropertyNotified(a1, "value", "Hello", "Coucou", listener);
		assertPropertyNotified(b1, "u1", "Bonjour", "Bijour", listener);
		assertPropertyNotified(b1, "u2", "Le monde !", "Le monde !!!", listener);

	}

	/**
	 * No structural modification of compared lists
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbedding1() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c12.setV1("Tom");
		c12.setV2("Jerry");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c12);
		a1.addToConceptCs(c13);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();
		ConceptC c21 = a2.getConceptCs().get(0);
		ConceptC c22 = a2.getConceptCs().get(1);
		ConceptC c23 = a2.getConceptCs().get(2);

		assertTrue(a1.equalsObject(a2));
		assertTrue(b1.equalsObject(b2));
		assertTrue(c11.equalsObject(c21));
		assertTrue(c12.equalsObject(c22));
		assertTrue(c13.equalsObject(c23));

		c21.setV1("Riri2");
		assertFalse(c11.equalsObject(c21));
		assertFalse(a1.equalsObject(a2));

		assertEquals(0.066, getDistance(factory, c11, c21), 0.01);
		assertEquals(0.0, getDistance(factory, b1, b2), 0.01);
		assertEquals(0.011, getDistance(factory, a1, a2), 0.01);

		c21.setV1("Donald");
		assertFalse(c11.equalsObject(c21));
		assertFalse(a1.equalsObject(a2));

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(0.333, getDistance(factory, c11, c21), 0.01);
		assertEquals(0.0, getDistance(factory, b1, b2), 0.01);
		assertEquals(0.055, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c11.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c12.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c13.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals("Donald", c11.getV1());

		assertEquals(1, listener.events.size());

		assertPropertyNotified(c11, "v1", "Riri", "Donald", listener);

	}

	/**
	 * We try to match lists with a value to be added at the top of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingAddingFirst() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c12.setV1("Tom");
		c12.setV2("Jerry");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c12);
		a1.addToConceptCs(c13);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		ConceptC c22 = a2.getConceptCs().get(0);
		c22.setV1("Tom2");
		ConceptC c23 = a2.getConceptCs().get(1);
		c23.setV2("Julien2");

		ConceptC c21 = factory.newInstance(ConceptC.class);
		c21.setV1("Riri");
		c21.setV2("Fifi");
		c21.setV3("Loulou");
		a2.addToConceptCs(c21);
		a2.moveConceptCToIndex(c21, 0);

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.195, getDistance(factory, a1, a2), 0.01);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c12.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c13.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(3, a1.getConceptCs().size());
		ConceptC c11 = a1.getConceptCs().get(0);
		assertTrue(a1.equalsObject(a2));
		assertTrue(c11.equalsObject(c21));

		assertEquals(5, listener.events.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, c11, listener); // Creation of c11
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 1, 2, listener); // Index move
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 0, 1, listener); // Index move
		assertPropertyNotified(c12, ConceptC.V1, "Tom", "Tom2", listener); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2", listener); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be added in the middle of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingAddingMiddle() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c13);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		ConceptC c21 = a2.getConceptCs().get(0);
		c21.setV1("Riri2");
		ConceptC c23 = a2.getConceptCs().get(1);
		c23.setV2("Julien2");

		ConceptC c22 = factory.newInstance(ConceptC.class);
		c22.setV1("Tom");
		c22.setV2("Jerry");
		a2.addToConceptCs(c22);
		a2.moveConceptCToIndex(c22, 1);

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.195, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c11.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c13.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(3, a1.getConceptCs().size());
		ConceptC c12 = a1.getConceptCs().get(1);
		assertTrue(a1.equalsObject(a2));
		assertTrue(c12.equalsObject(c22));

		assertEquals(4, listener.events.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, c12, listener); // Creation of c12
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 1, 2, listener); // Index move
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2", listener); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2", listener); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be added at the end of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingAddingEnd() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c12.setV1("Tom");
		c12.setV2("Jerry");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c12);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		ConceptC c21 = a2.getConceptCs().get(0);
		c21.setV1("Riri2");
		ConceptC c22 = a2.getConceptCs().get(1);
		c22.setV2("Jerry2");

		ConceptC c23 = factory.newInstance(ConceptC.class);
		c23.setV1("XTof");
		c23.setV2("Julien");
		c23.setV3("Sylvain");
		a2.addToConceptCs(c23);

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.191, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c11.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c12.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(3, a1.getConceptCs().size());
		ConceptC c13 = a1.getConceptCs().get(2);
		assertTrue(a1.equalsObject(a2));
		assertTrue(c13.equalsObject(c23));

		assertEquals(3, listener.events.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, c13, listener); // Creation of c13
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2", listener); // Modification of v1
		assertPropertyNotified(c12, ConceptC.V2, "Jerry", "Jerry2", listener); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be removed at the top of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingRemovingFirst() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c12.setV1("Tom");
		c12.setV2("Jerry");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c12);
		a1.addToConceptCs(c13);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		ConceptC c21 = a2.getConceptCs().get(0);
		ConceptC c22 = a2.getConceptCs().get(1);
		ConceptC c23 = a2.getConceptCs().get(2);

		a2.removeFromConceptCs(c21);
		c22.setV1("Tom2");
		c23.setV2("Julien2");

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.195, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c12.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c13.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(2, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, listener.events.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, c11, null, listener); // Deletion of c11
		assertPropertyNotified(c12, ConceptC.V1, "Tom", "Tom2", listener); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2", listener); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be removed in the middle of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingRemovingMiddle() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c12.setV1("Tom");
		c12.setV2("Jerry");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c12);
		a1.addToConceptCs(c13);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		ConceptC c21 = a2.getConceptCs().get(0);
		ConceptC c22 = a2.getConceptCs().get(1);
		ConceptC c23 = a2.getConceptCs().get(2);

		a2.removeFromConceptCs(c22);
		c21.setV1("Riri2");
		c23.setV2("Julien2");

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.195, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c11.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c13.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(2, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, listener.events.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, c12, null, listener); // Deletion of c12
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2", listener); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2", listener); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be removed at the end of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingRemovingEnd() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c12.setV1("Tom");
		c12.setV2("Jerry");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c12);
		a1.addToConceptCs(c13);

		ConceptA a2 = (ConceptA) a1.cloneObject();
		ConceptB b2 = a2.getConceptB();

		ConceptC c21 = a2.getConceptCs().get(0);
		ConceptC c22 = a2.getConceptCs().get(1);
		ConceptC c23 = a2.getConceptCs().get(2);

		a2.removeFromConceptCs(c23);
		c21.setV1("Riri2");
		c22.setV1("Tom2");

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.198, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c11.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c12.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(2, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, listener.events.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, c13, null, listener); // Deletion of c13
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2", listener); // Modification of v1
		assertPropertyNotified(c12, ConceptC.V1, "Tom", "Tom2", listener); // Modification of v1

	}

	/**
	 * We try to match lists with heterogeneous types
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingAvoidUpdatingWithDifferentTypes() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		a1.setValue("Hello");

		ConceptC1 i1 = factory.newInstance(ConceptC1.class);
		i1.setV1("First instance");
		i1.setV5("Hello world !");
		a1.addToConceptCs(i1);

		ConceptA a2 = factory.newInstance(ConceptA.class);
		a2.setValue("Hello guy !");

		ConceptC2 i2 = factory.newInstance(ConceptC2.class);
		i2.setV1("Second instance");
		i2.setV6("A specific value for ConceptC2");
		a2.addToConceptCs(i2);

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.772, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(1, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, listener.events.size());

		assertPropertyNotified(a1, ConceptA.VALUE, "Hello", "Hello guy !", listener); // Updating of value
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, i1, null, listener); // Deletion of i1
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, i2, listener); // Adding of i2

	}

	/**
	 * We try to match lists with heterogeneous types
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingReorder() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		a1.setValue("Hello");

		ConceptC1 i1 = factory.newInstance(ConceptC1.class);
		i1.setV1("First instance");
		i1.setV5("Hello world !");
		a1.addToConceptCs(i1);

		ConceptC2 i2 = factory.newInstance(ConceptC2.class);
		i2.setV1("Second instance");
		i2.setV6("A specific value for ConceptC2");
		a1.addToConceptCs(i2);

		ConceptA a2 = factory.newInstance(ConceptA.class);
		a2.setValue("Hello guy !");

		ConceptC2 j2 = factory.newInstance(ConceptC2.class);
		j2.setV1("Second instance");
		j2.setV6("A specific value for ConceptC2");
		a2.addToConceptCs(j2);

		ConceptC1 j1 = factory.newInstance(ConceptC1.class);
		j1.setV1("First instance");
		j1.setV5("Hello world !");
		a2.addToConceptCs(j1);

		assertFalse(a1.equalsObject(a2));

		assertEquals(0.181, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertTrue(a1.equalsObject(a2));

		assertEquals(2, listener.events.size());

		assertPropertyNotified(a1, ConceptA.VALUE, "Hello", "Hello guy !", listener); // Updating of value
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 0, 1, listener); // Move index

	}

	/**
	 * We try to match lists with heterogeneous types
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingHeterogeneousContext() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		a1.setValue("Hello");

		ConceptC1 i1 = factory.newInstance(ConceptC1.class);
		i1.setV1("First instance");
		i1.setV5("Hello world !");
		a1.addToConceptCs(i1);

		ConceptC2 i2 = factory.newInstance(ConceptC2.class);
		i2.setV1("Second instance");
		i2.setV6("A specific value for ConceptC2");
		a1.addToConceptCs(i2);

		ConceptC2 i3 = factory.newInstance(ConceptC2.class);
		i3.setV1("Third instance");
		i3.setV6("Another specific value for ConceptC2");
		a1.addToConceptCs(i3);

		ConceptC1 i4 = factory.newInstance(ConceptC1.class);
		i4.setV1("Fourth instance");
		i4.setV5("Nice weather today");
		a1.addToConceptCs(i4);

		ConceptC1 i5 = factory.newInstance(ConceptC1.class);
		i5.setV1("Fifth instance");
		i5.setV5("Hello");
		a1.addToConceptCs(i5);

		ConceptC1 i6 = factory.newInstance(ConceptC1.class);
		i6.setV1("Sixth instance");
		i6.setV5("Coucou");
		a1.addToConceptCs(i6);

		ConceptC2 i7 = factory.newInstance(ConceptC2.class);
		i7.setV1("Seventh instance");
		i7.setV6("Another specific value for ConceptC2");
		a1.addToConceptCs(i7);

		ConceptA a2 = factory.newInstance(ConceptA.class);
		a2.setValue("Hello guy !");

		ConceptC2 j3 = factory.newInstance(ConceptC2.class);
		j3.setV1("A Third instance");
		j3.setV6("Another specific value for ConceptC2");
		a2.addToConceptCs(j3);

		ConceptC1 j4 = factory.newInstance(ConceptC1.class);
		j4.setV1("Fourth instance bis");
		j4.setV5("Nice weather today");
		a2.addToConceptCs(j4);

		ConceptC1 j5 = factory.newInstance(ConceptC1.class);
		j5.setV1("Fifth instance bis");
		j5.setV5("Hello");
		a2.addToConceptCs(j5);

		ConceptC1 j8 = factory.newInstance(ConceptC1.class);
		j8.setV1("Height instance");
		j8.setV5("Hello world");
		a2.addToConceptCs(j8);

		assertFalse(a1.equalsObject(a2));

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(0.504, getDistance(factory, a1, a2), 0.01);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i2.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i3.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i4.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i5.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i6.getPropertyChangeSupport().addPropertyChangeListener(listener);
		i7.getPropertyChangeSupport().addPropertyChangeListener(listener);

		a1.updateWith(a2);

		assertEquals(4, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		/*System.out.println("---------> Logs for failing test");
		for (PropertyChangeEvent event : listener.events) {
			System.out.println("Event " + event.getPropertyName() + " old=" + event.getOldValue() + " new=" + event.getNewValue()
					+ " source=" + event.getSource());
		}*/

		assertEquals(12, listener.events.size());

		assertPropertyNotified(a1, ConceptA.VALUE, "Hello", "Hello guy !", listener); // Updating of value
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, i2, null, listener); // Deletion of i2
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, i6, null, listener); // Deletion of i6
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, i7, null, listener); // Deletion of i7
		assertPropertyNotified(i3, ConceptC2.V1, "Third instance", "A Third instance", listener); // Change of V1
		assertPropertyNotified(i4, ConceptC1.V1, "Fourth instance", "Fourth instance bis", listener); // Change of V1
		assertPropertyNotified(i5, ConceptC1.V1, "Fifth instance", "Fifth instance bis", listener); // Change of V1
		assertPropertyNotified(i1, ConceptC1.V5, "Hello world !", "Hello world", listener); // Change of V5
		assertPropertyNotified(i1, ConceptC1.V1, "First instance", "Height instance", listener); // Change of V1
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 1, 0, listener); // reindex
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 2, 1, listener); // reindex
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 3, 2, listener); // reindex

	}

}
