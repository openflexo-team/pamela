package org.openflexo.pamela.test.updatewith2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.CompareAndMergeUtils;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Test PAMELA updateWith(Object) feature
 * 
 * @author sylvain
 * 
 */
public class TestCyclicUpdateWith {

	private static PamelaModelFactory createFactory() {
		try {
			return new PamelaModelFactory(PamelaMetaModelLibrary.getCompoundModelContext(ConceptA.class, ConceptB.class, ConceptC.class));
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

	private static double getDistance(PamelaModelFactory factory, Object o1, Object o2) {
		return CompareAndMergeUtils.getDistance(factory, o1, o2);
	}

	@Test
	public void testCyclicUpdating() throws Exception {
		PamelaModelFactory factory = createFactory();

		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		ConceptC c1 = factory.newInstance(ConceptC.class);
		a1.setValue("a1");
		b1.setValue("b1");
		c1.setValue("c1");
		a1.setConceptB(b1);
		b1.setConceptC(c1);
		c1.setConceptA(a1);

		ConceptA a2 = factory.newInstance(ConceptA.class);
		ConceptB b2 = factory.newInstance(ConceptB.class);
		ConceptC c2 = factory.newInstance(ConceptC.class);
		a2.setValue("a2");
		b2.setValue("b2");
		c2.setValue("c2");
		a2.setConceptB(b2);
		b2.setConceptC(c2);
		c2.setConceptA(a2);

		// We track events on a1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		c1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		// assertTrue(a1.equalsObject(a2)); // Pamela compare
		// assertFalse(a1.equals(a2)); // Java compare

		System.out.println("Avant update");
		System.out.println(factory.stringRepresentation(a1));

		// Updating
		System.out.println("On update");
		a1.updateWith(a2);
		System.out.println(factory.stringRepresentation(a1));

		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare

		System.out.println("Events: " + listener.events);
		assertEquals(3, listener.events.size());
		assertPropertyNotified(a1, "value", "a1", "a2", listener);
		assertPropertyNotified(b1, "value", "b1", "b2", listener);
		assertPropertyNotified(c1, "value", "c1", "c2", listener);

		// Test update the value of ConceptA

		a1.setValue("Une valeur");

		a2.setValue("Une valeur");
		assertTrue(a1.equalsObject(a2));
		assertEquals(0.0, getDistance(factory, a1, a2), 0.01);

		a2.setValue("Une valeur2");
		assertFalse(a1.equalsObject(a2));

		assertEquals(0.03, getDistance(factory, a1, a2), 0.01);

		a2.setValue("Une autre valeur");
		assertFalse(a1.equalsObject(a2));
		assertEquals(0.125, getDistance(factory, a1, a2), 0.01);

		TestChangeListener updateListener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(updateListener);
		a1.updateWith(a2);

		assertEquals("Une autre valeur", a1.getValue());

		assertEquals(1, updateListener.events.size());
		assertPropertyNotified(a1, "value", "Une valeur", "Une autre valeur", listener);
	}

}
