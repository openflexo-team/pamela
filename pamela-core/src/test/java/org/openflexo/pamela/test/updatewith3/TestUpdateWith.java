package org.openflexo.pamela.test.updatewith3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflexo.pamela.ModelContextLibrary;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.CompareAndMergeUtils;
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
			return new ModelFactory(ModelContextLibrary.getCompoundModelContext(ConceptA.class, ConceptB.class));
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
		return CompareAndMergeUtils.getDistance(factory, o1, o2);
	}

	@Test
	public void testSimpleUpdating() throws Exception {
		ModelFactory factory = createFactory();
		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("A");
		a1.setConceptB(b1);
		b1.setValue("B");
		b1.setConceptA(a1);

		ConceptA a2 = factory.newInstance(ConceptA.class);
		ConceptB b2 = factory.newInstance(ConceptB.class);
		a2.setValue("A");
		a2.setConceptB(b2);
		b2.setValue("B");
		b2.setConceptA(a2);

		// We track events on a1 and b1
		TestChangeListener listener = new TestChangeListener();
		a1.getPropertyChangeSupport().addPropertyChangeListener(listener);
		b1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare

		// Same object !
		a1.updateWith(a2);
		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare
		assertEquals(0, listener.events.size());

	}

}
