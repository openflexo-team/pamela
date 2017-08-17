package org.flexo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model8.ConceptA;
import org.openflexo.model8.ConceptB;
import org.openflexo.model8.ConceptC;

/**
 * Test PAMELA updateWith(Object) feature
 * 
 * @author sylvain
 * 
 */
public class TestUpdateWith implements PropertyChangeListener {

	/**
	 * Test the factory
	 */
	@Before
	public void testFactory() {

		try {
			factory = new ModelFactory(ModelContextLibrary.getCompoundModelContext(ConceptA.class));

			ModelEntity<ConceptA> conceptAEntity = factory.getModelContext().getModelEntity(ConceptA.class);
			ModelEntity<ConceptB> conceptBEntity = factory.getModelContext().getModelEntity(ConceptB.class);
			ModelEntity<ConceptC> conceptCEntity = factory.getModelContext().getModelEntity(ConceptC.class);

			assertNotNull(conceptAEntity);
			assertNotNull(conceptBEntity);
			assertNotNull(conceptCEntity);

		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail();
		}
	}

	private ModelFactory factory;
	private List<PropertyChangeEvent> evts = new ArrayList<>();

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// System.out.println("Received notification " + evt.getPropertyName() + " de " + evt.getOldValue() + " a " + evt.getNewValue());
		if (evt.getPropertyName().equals("serializing")) {
			return;
		}
		evts.add(evt);
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

	private void assertPropertyNotified(Object source, String propertyName, Object oldValue, Object newValue) {
		for (PropertyChangeEvent evt : evts) {
			if (evt.getSource().equals(source) && evt.getPropertyName().equals(propertyName) && isEqual(evt.getOldValue(), oldValue)
					&& isEqual(evt.getNewValue(), newValue)) {
				return;
			}
		}
		fail("Property notification " + propertyName + " from " + oldValue + " to " + newValue + " not fired !");
	}

	private double getDistance(Object o1, Object o2) {
		return factory.getHandler(o1).getDistance(o2);
	}

	@Test
	public void testSimpleUpdating() throws Exception {

		assertEquals(0, evts.size());

		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptA a2 = factory.newInstance(ConceptA.class);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);

		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare

		// Same object !
		a1.updateWith(a2);
		assertTrue(a1.equalsObject(a2)); // Pamela compare
		assertFalse(a1.equals(a2)); // Java compare
		assertEquals(0, evts.size());

		// Test update the value of ConceptA

		a1.setValue("Une valeur");

		a2.setValue("Une valeur");
		assertTrue(a1.equalsObject(a2));
		assertEquals(0.0, getDistance(a1, a2), 0.01);

		a2.setValue("Une valeur2");
		assertFalse(a1.equalsObject(a2));
		assertEquals(0.09, getDistance(a1, a2), 0.01);

		a2.setValue("Une autre valeur");
		assertFalse(a1.equalsObject(a2));
		assertEquals(0.375, getDistance(a1, a2), 0.01);

		evts.clear();

		a1.updateWith(a2);

		assertEquals("Une autre valeur", a1.getValue());

		assertEquals(1, evts.size());
		assertPropertyNotified(a1, "value", "Une valeur", "Une autre valeur");
	}

	@Test
	public void testUpdatingWithEmbedding() throws Exception {

		assertEquals(0, evts.size());

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
		assertEquals(0.277, getDistance(a1, a2), 0.01);

		assertTrue(b1.equalsObject(b2));
		assertEquals(0.0, getDistance(b1, b2), 0.01);

		b2.setU1("Bijour");
		b2.setU2("Le monde !!!");

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertFalse(b1.equalsObject(b2));
		assertEquals(0.226, getDistance(b1, b2), 0.01);

		assertFalse(a1.equalsObject(a2));
		assertEquals(0.428, getDistance(a1, a2), 0.01);
		// System.out.println("d1: " + factory.getHandler(a1).getDistanceBetweenValues(a1.getValue(), a2.getValue()));
		// System.out.println("d2: " + factory.getHandler(a1).getDistanceBetweenValues(a1.getConceptB(), a2.getConceptB()));

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);

		a1.updateWith(a2);

		assertEquals("Coucou", a1.getValue());
		assertEquals("Bijour", b1.getU1());
		assertEquals("Le monde !!!", b1.getU2());

		assertEquals(3, evts.size());

		assertPropertyNotified(a1, "value", "Hello", "Coucou");
		assertPropertyNotified(b1, "u1", "Bonjour", "Bijour");
		assertPropertyNotified(b1, "u2", "Le monde !", "Le monde !!!");

	}

	/**
	 * No structural modification of compared lists
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbedding1() throws Exception {

		assertEquals(0, evts.size());

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

		System.out.println("a1=" + factory.stringRepresentation(a1));
		System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(0.066, getDistance(c11, c21), 0.01);
		assertEquals(0.0, getDistance(b1, b2), 0.01);
		assertEquals(0.011, getDistance(a1, a2), 0.01);

		c21.setV1("Donald");
		assertFalse(c11.equalsObject(c21));
		assertFalse(a1.equalsObject(a2));

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(0.333, getDistance(c11, c21), 0.01);
		assertEquals(0.0, getDistance(b1, b2), 0.01);
		assertEquals(0.055, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c11.getPropertyChangeSupport().addPropertyChangeListener(this);
		c12.getPropertyChangeSupport().addPropertyChangeListener(this);
		c13.getPropertyChangeSupport().addPropertyChangeListener(this);

		a1.updateWith(a2);

		assertEquals("Donald", c11.getV1());

		assertEquals(1, evts.size());

		assertPropertyNotified(c11, "v1", "Riri", "Donald");

	}

	/**
	 * We try to match lists with a value to be added at the top of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingAddingFirst() throws Exception {

		assertEquals(0, evts.size());

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

		assertEquals(0.195, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c12.getPropertyChangeSupport().addPropertyChangeListener(this);
		c13.getPropertyChangeSupport().addPropertyChangeListener(this);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		a1.updateWith(a2);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(3, a1.getConceptCs().size());
		ConceptC c11 = a1.getConceptCs().get(0);
		assertTrue(a1.equalsObject(a2));
		assertTrue(c11.equalsObject(c21));

		assertEquals(4, evts.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, c11); // Creation of c11
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 2, 0); // Index move
		assertPropertyNotified(c12, ConceptC.V1, "Tom", "Tom2"); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2"); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be added in the middle of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingAddingMiddle() throws Exception {

		assertEquals(0, evts.size());

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

		assertEquals(0.195, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c11.getPropertyChangeSupport().addPropertyChangeListener(this);
		c13.getPropertyChangeSupport().addPropertyChangeListener(this);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		a1.updateWith(a2);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(3, a1.getConceptCs().size());
		ConceptC c12 = a1.getConceptCs().get(1);
		assertTrue(a1.equalsObject(a2));
		assertTrue(c12.equalsObject(c22));

		assertEquals(4, evts.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, c12); // Creation of c12
		assertPropertyNotified(a1, ConceptA.CONCEPT_C, 2, 1); // Index move
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2"); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2"); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be added at the end of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingAddingEnd() throws Exception {

		assertEquals(0, evts.size());

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

		assertEquals(0.191, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c11.getPropertyChangeSupport().addPropertyChangeListener(this);
		c12.getPropertyChangeSupport().addPropertyChangeListener(this);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		a1.updateWith(a2);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(3, a1.getConceptCs().size());
		ConceptC c13 = a1.getConceptCs().get(2);
		assertTrue(a1.equalsObject(a2));
		assertTrue(c13.equalsObject(c23));

		assertEquals(3, evts.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, null, c13); // Creation of c13
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2"); // Modification of v1
		assertPropertyNotified(c12, ConceptC.V2, "Jerry", "Jerry2"); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be removed at the top of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingRemovingFirst() throws Exception {

		assertEquals(0, evts.size());

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

		assertEquals(0.195, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c12.getPropertyChangeSupport().addPropertyChangeListener(this);
		c13.getPropertyChangeSupport().addPropertyChangeListener(this);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		a1.updateWith(a2);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(2, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, evts.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, c11, null); // Deletion of c11
		assertPropertyNotified(c12, ConceptC.V1, "Tom", "Tom2"); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2"); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be removed in the middle of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingRemovingMiddle() throws Exception {

		assertEquals(0, evts.size());

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

		assertEquals(0.195, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c11.getPropertyChangeSupport().addPropertyChangeListener(this);
		c13.getPropertyChangeSupport().addPropertyChangeListener(this);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		a1.updateWith(a2);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(2, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, evts.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, c12, null); // Deletion of c12
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2"); // Modification of v1
		assertPropertyNotified(c13, ConceptC.V2, "Julien", "Julien2"); // Modification of v2

	}

	/**
	 * We try to match lists with a value to be removed at the end of the list
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdatingWithMultipleEmbeddingRemovingEnd() throws Exception {

		assertEquals(0, evts.size());

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

		assertEquals(0.198, getDistance(a1, a2), 0.01);

		// We track events on a1
		a1.getPropertyChangeSupport().addPropertyChangeListener(this);
		b1.getPropertyChangeSupport().addPropertyChangeListener(this);
		c11.getPropertyChangeSupport().addPropertyChangeListener(this);
		c12.getPropertyChangeSupport().addPropertyChangeListener(this);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		a1.updateWith(a2);

		// System.out.println("a1=" + factory.stringRepresentation(a1));
		// System.out.println("a2=" + factory.stringRepresentation(a2));

		assertEquals(2, a1.getConceptCs().size());
		assertTrue(a1.equalsObject(a2));

		assertEquals(3, evts.size());

		assertPropertyNotified(a1, ConceptA.CONCEPT_C, c13, null); // Deletion of c13
		assertPropertyNotified(c11, ConceptC.V1, "Riri", "Riri2"); // Modification of v1
		assertPropertyNotified(c12, ConceptC.V1, "Tom", "Tom2"); // Modification of v1

	}
}
