package org.openflexo.pamela.test.updatewith4;

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
	public void testUpdating1() throws Exception {
		PamelaModelFactory factory = createFactory();
		ParentConcept initialParent = factory.newInstance(ParentConcept.class);
		initialParent.setValue("A");
		ChildConcept initialChild1 = factory.newInstance(ChildConcept.class);
		initialChild1.setValue("B");
		ChildConcept initialChild2 = factory.newInstance(ChildConcept.class);
		initialChild2.setValue("C");
		initialParent.addToChildren(initialChild1);
		initialParent.addToChildren(initialChild2);
		assertSame(initialParent, initialChild1.getParent());
		assertSame(initialParent, initialChild2.getParent());

		ParentConcept modifiedParent = factory.newInstance(ParentConcept.class);
		modifiedParent.setValue("A");
		ChildConcept modifiedChild1 = factory.newInstance(ChildConcept.class);
		modifiedChild1.setValue("B");
		ChildConcept modifiedChild2 = factory.newInstance(ChildConcept.class);
		modifiedChild2.setValue("C");
		modifiedParent.addToChildren(modifiedChild1);
		modifiedParent.addToChildren(modifiedChild2);
		assertSame(modifiedParent, modifiedChild1.getParent());
		assertSame(modifiedParent, modifiedChild2.getParent());

		// We track events
		TestChangeListener listener = new TestChangeListener();
		initialParent.getPropertyChangeSupport().addPropertyChangeListener(listener);
		// child1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		// We update initialParent with modifiedParent (and the new child)
		initialParent.updateWith(modifiedParent);

		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		assertTrue(initialChild1.equalsObject(modifiedChild1)); // Pamela compare
		assertFalse(initialChild1.equals(modifiedChild1)); // Java compare

		assertTrue(initialChild2.equalsObject(modifiedChild2)); // Pamela compare
		assertFalse(initialChild2.equals(modifiedChild2)); // Java compare

		// assertEquals(1, listener.events.size());
		// listener.assertEvent(0, ParentConcept.CHILDREN, null, initialChild1);

		assertEquals(2, initialParent.getChildren().size());
		assertSame(initialParent.getChildren().get(0), initialChild1);
		assertSame(initialParent.getChildren().get(1), initialChild2);
		assertSame(initialChild1.getParent(), initialParent);
		assertSame(initialChild2.getParent(), initialParent);
		assertEquals("B", initialChild1.getValue());
		assertEquals("C", initialChild2.getValue());

	}

	@Test
	public void testUpdating2() throws Exception {
		PamelaModelFactory factory = createFactory();
		ParentConcept initialParent = factory.newInstance(ParentConcept.class);
		initialParent.setValue("A");
		ChildConcept initialChild1 = factory.newInstance(ChildConcept.class);
		initialChild1.setValue("B");
		ChildConcept initialChild2 = factory.newInstance(ChildConcept.class);
		initialChild2.setValue("C");
		initialParent.addToChildren(initialChild1);
		initialParent.addToChildren(initialChild2);
		assertSame(initialParent, initialChild1.getParent());
		assertSame(initialParent, initialChild2.getParent());

		ParentConcept modifiedParent = factory.newInstance(ParentConcept.class);
		modifiedParent.setValue("A");
		ChildConcept modifiedChild1 = factory.newInstance(ChildConcept.class);
		modifiedChild1.setValue("B1");
		ChildConcept modifiedChild2 = factory.newInstance(ChildConcept.class);
		modifiedChild2.setValue("C1");
		modifiedParent.addToChildren(modifiedChild1);
		modifiedParent.addToChildren(modifiedChild2);
		assertSame(modifiedParent, modifiedChild1.getParent());
		assertSame(modifiedParent, modifiedChild2.getParent());

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

		assertTrue(initialChild1.equalsObject(modifiedChild1)); // Pamela compare
		assertFalse(initialChild1.equals(modifiedChild1)); // Java compare

		assertTrue(initialChild2.equalsObject(modifiedChild2)); // Pamela compare
		assertFalse(initialChild2.equals(modifiedChild2)); // Java compare

		// assertEquals(1, listener.events.size());
		// listener.assertEvent(0, ParentConcept.CHILDREN, null, initialChild1);

		assertEquals(2, initialParent.getChildren().size());
		assertSame(initialParent.getChildren().get(0), initialChild1);
		assertSame(initialParent.getChildren().get(1), initialChild2);
		assertSame(initialChild1.getParent(), initialParent);
		assertSame(initialChild2.getParent(), initialParent);
		assertEquals("B1", initialChild1.getValue());
		assertEquals("C1", initialChild2.getValue());

	}

	@Test
	public void testUpdating3() throws Exception {
		PamelaModelFactory factory = createFactory();
		ParentConcept initialParent = factory.newInstance(ParentConcept.class);
		initialParent.setValue("A");
		ChildConcept initialChild1 = factory.newInstance(ChildConcept.class);
		initialChild1.setValue("B");
		initialChild1.setValue2("Value2ForB");
		ChildConcept initialChild2 = factory.newInstance(ChildConcept.class);
		initialChild2.setValue("C");
		initialChild2.setValue2("Value2ForC");
		initialParent.addToChildren(initialChild1);
		initialParent.addToChildren(initialChild2);
		assertSame(initialParent, initialChild1.getParent());
		assertSame(initialParent, initialChild2.getParent());

		ParentConcept modifiedParent = factory.newInstance(ParentConcept.class);
		modifiedParent.setValue("A");
		ChildConcept modifiedChild1 = factory.newInstance(ChildConcept.class);
		modifiedChild1.setValue("B");
		modifiedChild1.setValue2("Value2ForB");
		ChildConcept modifiedChild2 = factory.newInstance(ChildConcept.class);
		modifiedChild2.setValue("C");
		modifiedChild2.setValue2("Value2ForC");
		modifiedParent.addToChildren(modifiedChild1);
		modifiedParent.addToChildren(modifiedChild2);
		assertSame(modifiedParent, modifiedChild1.getParent());
		assertSame(modifiedParent, modifiedChild2.getParent());

		// We track events
		TestChangeListener listener = new TestChangeListener();
		initialParent.getPropertyChangeSupport().addPropertyChangeListener(listener);
		// child1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		// We update initialParent with modifiedParent (and the new child)
		initialParent.updateWith(modifiedParent);

		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		assertTrue(initialChild1.equalsObject(modifiedChild1)); // Pamela compare
		assertFalse(initialChild1.equals(modifiedChild1)); // Java compare

		assertTrue(initialChild2.equalsObject(modifiedChild2)); // Pamela compare
		assertFalse(initialChild2.equals(modifiedChild2)); // Java compare

		// assertEquals(1, listener.events.size());
		// listener.assertEvent(0, ParentConcept.CHILDREN, null, initialChild1);

		assertEquals(2, initialParent.getChildren().size());
		assertSame(initialParent.getChildren().get(0), initialChild1);
		assertSame(initialParent.getChildren().get(1), initialChild2);
		assertSame(initialChild1.getParent(), initialParent);
		assertSame(initialChild2.getParent(), initialParent);
		assertEquals("B", initialChild1.getValue());
		assertEquals("C", initialChild2.getValue());

	}

	@Test
	public void testUpdating4() throws Exception {
		PamelaModelFactory factory = createFactory();
		ParentConcept initialParent = factory.newInstance(ParentConcept.class);
		initialParent.setValue("A");
		ChildConcept initialChild1 = factory.newInstance(ChildConcept.class);
		initialChild1.setValue("B");
		ChildConcept initialChild2 = factory.newInstance(ChildConcept.class);
		initialChild2.setValue("C");
		initialParent.addToChildren(initialChild1);
		initialParent.addToChildren(initialChild2);
		assertSame(initialParent, initialChild1.getParent());
		assertSame(initialParent, initialChild2.getParent());

		ParentConcept modifiedParent = factory.newInstance(ParentConcept.class);
		modifiedParent.setValue("A");
		ChildConcept modifiedChild1 = factory.newInstance(ChildConcept.class);
		modifiedChild1.setValue("B");
		ChildConcept modifiedChild2 = factory.newInstance(ChildConcept.class);
		modifiedChild2.setValue("C");
		modifiedParent.addToChildren(modifiedChild1);
		modifiedParent.addToChildren(modifiedChild2);
		assertSame(modifiedParent, modifiedChild1.getParent());
		assertSame(modifiedParent, modifiedChild2.getParent());
		modifiedChild1.addToSiblings(modifiedChild2);
		modifiedChild2.addToSiblings(modifiedChild1);

		// We track events
		TestChangeListener listener = new TestChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				super.propertyChange(evt);
				if (evt.getPropertyName().equals("children")) {
					System.out.println(
							"Received event " + evt.getPropertyName() + " oldValue: " + evt.getOldValue() + " to " + evt.getNewValue());
					Thread.dumpStack();
				}
			}
		};
		initialParent.getPropertyChangeSupport().addPropertyChangeListener(listener);
		// child1.getPropertyChangeSupport().addPropertyChangeListener(listener);

		assertFalse(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		// We update initialParent with modifiedParent (and the new child)
		System.out.println("initialParent=" + initialParent);
		System.out.println("  initialChild1=" + initialChild1);
		System.out.println("  initialChild2=" + initialChild2);
		System.out.println("modifiedParent=" + modifiedParent);
		System.out.println("  modifiedChild1=" + modifiedChild1);
		System.out.println("  modifiedChild2=" + modifiedChild2);

		initialParent.updateWith(modifiedParent);

		assertTrue(initialParent.equalsObject(modifiedParent)); // Pamela compare
		assertFalse(initialParent.equals(modifiedParent)); // Java compare

		assertTrue(initialChild1.equalsObject(modifiedChild1)); // Pamela compare
		assertFalse(initialChild1.equals(modifiedChild1)); // Java compare

		assertTrue(initialChild2.equalsObject(modifiedChild2)); // Pamela compare
		assertFalse(initialChild2.equals(modifiedChild2)); // Java compare

		// assertEquals(1, listener.events.size());
		// listener.assertEvent(0, ParentConcept.CHILDREN, null, initialChild1);

		assertEquals(2, initialParent.getChildren().size());
		assertSame(initialParent.getChildren().get(0), initialChild1);
		assertSame(initialParent.getChildren().get(1), initialChild2);
		assertSame(initialChild1.getParent(), initialParent);
		assertSame(initialChild2.getParent(), initialParent);
		assertEquals("B", initialChild1.getValue());
		assertEquals("C", initialChild2.getValue());

		assertEquals(1, initialChild1.getSiblings().size());
		assertSame(initialChild1.getSiblings().get(0), initialChild2);

		assertEquals(1, initialChild2.getSiblings().size());
		assertSame(initialChild2.getSiblings().get(0), initialChild1);

	}

}
