package org.openflexo.pamela.test.updatewith3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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
public class TestUpdateWith {

	private static PamelaModelFactory createFactory() {
		try {
			return new PamelaModelFactory(PamelaMetaModelLibrary.getCompoundModelContext(ParentConcept.class, ChildConcept.class));
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
			// System.out.println("Received event " + evt.getPropertyName() + " oldValue: " + evt.getOldValue() + " to " +
			// evt.getNewValue());
			events.add(evt);
		}

		public void assertEvent(int index, String propertyName, Object fromValue, Object toValue) {
			assertTrue(index < events.size());
			PropertyChangeEvent e = events.get(index);
			assertEquals(propertyName, e.getPropertyName());
			assertSame(e.getOldValue(), fromValue);
			assertSame(e.getNewValue(), toValue);
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
	public void testUpdating() throws Exception {
		PamelaModelFactory factory = createFactory();
		ParentConcept initialParent = factory.newInstance(ParentConcept.class);
		initialParent.setValue("A");

		ParentConcept modifiedParent = factory.newInstance(ParentConcept.class);
		ChildConcept initialChild1 = factory.newInstance(ChildConcept.class);
		modifiedParent.setValue("A");
		modifiedParent.addToChildren(initialChild1);
		initialChild1.setValue("B1");
		initialChild1.setParent(modifiedParent);

		// We track events
		TestChangeListener listener = new TestChangeListener();
		initialParent.getPropertyChangeSupport().addPropertyChangeListener(listener);
		// child1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertFalse(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		// We update initialParent with modifiedParent (and the new child)
		initialParent.updateWith(modifiedParent);
		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare
		assertEquals(1, listener.events.size());
		listener.assertEvent(0, ParentConcept.CHILDREN, null, initialChild1);

		assertEquals(1, initialParent.getChildren().size());
		assertSame(initialParent.getChildren().get(0), initialChild1);
		assertSame(initialChild1.getParent(), initialParent);

		// Now we add 2 more children
		ParentConcept modifiedParent2 = factory.newInstance(ParentConcept.class);
		ChildConcept modifiedChild1 = factory.newInstance(ChildConcept.class);
		ChildConcept initialChild2 = factory.newInstance(ChildConcept.class);
		ChildConcept initialChild3 = factory.newInstance(ChildConcept.class);
		modifiedParent2.setValue("A");
		modifiedParent2.addToChildren(modifiedChild1);
		modifiedParent2.addToChildren(initialChild2);
		modifiedParent2.addToChildren(initialChild3);
		modifiedChild1.setValue("B1");
		modifiedChild1.setParent(modifiedParent2);
		initialChild2.setValue("B2");
		initialChild2.setParent(modifiedParent2);
		initialChild3.setValue("B3");
		initialChild3.setParent(modifiedParent2);

		// We update initialParent with modifiedParent (and the new child)
		initialParent.updateWith(modifiedParent2);

		assertTrue(initialParent.equalsObject(modifiedParent2)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent2)); // Java compare
		assertEquals(3, listener.events.size());
		listener.assertEvent(1, ParentConcept.CHILDREN, null, initialChild2);
		listener.assertEvent(2, ParentConcept.CHILDREN, null, initialChild3);

		assertEquals(3, initialParent.getChildren().size());
		assertSame(initialChild1, initialParent.getChildren().get(0));
		assertSame(initialChild2, initialParent.getChildren().get(1));
		assertSame(initialChild3, initialParent.getChildren().get(2));

		assertSame(initialChild1.getParent(), initialParent);
		assertSame(initialChild2.getParent(), initialParent);
		assertSame(initialChild3.getParent(), initialParent);

	}

	@Test
	public void testUpdating2() throws Exception {
		PamelaModelFactory factory = createFactory();
		ParentConcept initialParent = factory.newInstance(ParentConcept.class);
		initialParent.setValue("A");

		ParentConcept modifiedParent = factory.newInstance(ParentConcept.class);
		ChildConcept initialChild1 = factory.newInstance(ChildConcept.class);
		modifiedParent.setValue("A");
		modifiedParent.addToChildren(initialChild1);
		initialChild1.setValue("B1");
		initialChild1.setParent(modifiedParent);

		// We track events
		TestChangeListener listener = new TestChangeListener();
		initialParent.getPropertyChangeSupport().addPropertyChangeListener(listener);
		// child1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertFalse(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		// We update initialParent with modifiedParent (and the new child)
		initialParent.updateWith(modifiedParent);
		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare
		assertEquals(1, listener.events.size());
		listener.assertEvent(0, ParentConcept.CHILDREN, null, initialChild1);

		assertEquals(1, initialParent.getChildren().size());
		assertSame(initialParent.getChildren().get(0), initialChild1);
		assertSame(initialChild1.getParent(), initialParent);

		// Now we add 2 more children
		ParentConcept modifiedParent2 = factory.newInstance(ParentConcept.class);
		ChildConcept modifiedChild1 = factory.newInstance(ChildConcept.class);
		ChildConcept initialChild2 = factory.newInstance(ChildConcept.class);
		ChildConcept initialChild3 = factory.newInstance(ChildConcept.class);
		modifiedParent2.setValue("A");
		modifiedParent2.addToChildren(modifiedChild1);
		modifiedParent2.addToChildren(initialChild2);
		modifiedParent2.addToChildren(initialChild3);
		modifiedChild1.setValue("B1");
		modifiedChild1.setParent(modifiedParent2);
		initialChild2.setValue("B2");
		initialChild2.setParent(modifiedParent2);
		initialChild3.setValue("B3");
		initialChild3.setParent(modifiedParent2);

		modifiedChild1.addToSiblings(initialChild2);
		modifiedChild1.addToSiblings(initialChild3);

		initialChild2.addToSiblings(modifiedChild1);
		initialChild2.addToSiblings(initialChild3);

		initialChild3.addToSiblings(modifiedChild1);
		initialChild3.addToSiblings(initialChild2);

		// We update initialParent with modifiedParent (and the new child)
		initialParent.updateWith(modifiedParent2);

		assertTrue(initialParent.equalsObject(modifiedParent2)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent2)); // Java compare
		assertEquals(3, listener.events.size());
		listener.assertEvent(1, ParentConcept.CHILDREN, null, initialChild2);
		listener.assertEvent(2, ParentConcept.CHILDREN, null, initialChild3);

		assertEquals(3, initialParent.getChildren().size());
		assertSame(initialChild1, initialParent.getChildren().get(0));
		assertSame(initialChild2, initialParent.getChildren().get(1));
		assertSame(initialChild3, initialParent.getChildren().get(2));

		assertSame(initialChild1.getParent(), initialParent);
		assertSame(initialChild2.getParent(), initialParent);
		assertSame(initialChild3.getParent(), initialParent);

		assertEquals(2, initialChild1.getSiblings().size());
		assertSame(initialChild2, initialChild1.getSiblings().get(0));
		assertSame(initialChild3, initialChild1.getSiblings().get(1));

		assertEquals(2, initialChild2.getSiblings().size());
		assertSame(initialChild1, initialChild2.getSiblings().get(0));
		assertSame(initialChild3, initialChild2.getSiblings().get(1));

		assertEquals(2, initialChild3.getSiblings().size());
		assertSame(initialChild1, initialChild3.getSiblings().get(0));
		assertSame(initialChild2, initialChild3.getSiblings().get(1));

	}

}
