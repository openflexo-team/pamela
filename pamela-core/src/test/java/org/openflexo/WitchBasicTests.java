/**
 * 
 * Copyright (c) 2015, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelContextLibrary;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.EditingContextImpl;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.model.undo.UndoManager;
import org.openflexo.model.witchmodel.BurningObject;
import org.openflexo.model.witchmodel.Duck;
import org.openflexo.model.witchmodel.Person;
import org.openflexo.model.witchmodel.PhysicalObject;
import org.openflexo.model.witchmodel.WoodenObject;

public class WitchBasicTests extends AbstractPAMELATest {

	private ModelFactory factory;
	private ModelContext modelContext;
	private EditingContext editingContext;
	private UndoManager undoManager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		new File("/tmp").mkdirs();
		
		modelContext = ModelContextLibrary.getCompoundModelContext(PhysicalObject.class, Person.class, Duck.class, WoodenObject.class);
		factory = new ModelFactory(modelContext);

		editingContext = new EditingContextImpl();
		// TODO: ça c'est un peu pourri quand même!
		((EditingContextImpl) editingContext).createUndoManager();
		factory.setEditingContext(editingContext);
		undoManager = editingContext.getUndoManager();
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * We declare here a basic mapping model, and we check that the model construction is right
	 * 
	 * @throws Exception
	 */
	public void test1() throws Exception {

		// System.out.println(modelContext.debug());

		assertEquals(6, modelContext.getEntityCount());

	}

	public void test2() throws Exception {

		PhysicalObject apObject = factory.newInstance(PhysicalObject.class);
		assertTrue(apObject instanceof PhysicalObject);

		apObject.setDensity((float) 2.25);
		apObject.setWidth((float) 1);
		apObject.setHeight((float) 0.5);
		apObject.setLength((float) 0.25);

		System.out.println("Volume: " + apObject.getFullVolume() + " m3");
		System.out.println("Weight: " + apObject.getWeight() + " Kg\n\n");

		assertEquals((float) 0.125, apObject.getFullVolume().floatValue());
		assertEquals((float) (0.125 * 2.25 * 1000), apObject.getWeight().floatValue());

	}

	public void testWitch() throws Exception {

		Person apPerson = factory.newInstance(Person.class);
		Duck aDuck = factory.newInstance(Duck.class);
		assertTrue(apPerson instanceof Person);
		assertTrue(aDuck instanceof Duck);

		apPerson.setDensity((float) 100); // Should DO nothing
		apPerson.setWidth((float) 0.30);
		apPerson.setHeight((float) 1.5);
		apPerson.setLength((float) 0.20);
		assertEquals((float) 0.95, apPerson.getDensity());

		// Testing undo
		CompoundEdit initial = undoManager.startRecording("initial");
		apPerson.setName("Whalaou");
		undoManager.stopRecording(initial);
		CompoundEdit changename = undoManager.startRecording("changename");
		apPerson.setName("Okabunga");
		undoManager.stopRecording(changename);
		assertTrue(apPerson.getName().equals("Okabunga"));
		undoManager.undo();
		assertTrue(apPerson.getName().equals("Whalaou"));

		aDuck.setWidth((float) 0.30);
		aDuck.setHeight((float) 0.15);
		aDuck.setLength((float) 0.20);
		assertEquals((float) 0.3, aDuck.getDensity());

		System.out.println("Volume: " + apPerson.getFullVolume() + " m3");
		System.out.println("Weight: " + apPerson.getWeight() + " Kg");
		System.out.println("Sous l'eau de: " + apPerson.getInWaterDepth() + " m\n\n");

		System.out.println("Volume: " + aDuck.getFullVolume() + " m3");
		System.out.println("Weight: " + aDuck.getWeight() + " Kg");
		System.out.println("Sous l'eau de: " + aDuck.getInWaterDepth() + " m\n\n");

		if (apPerson.weighsSameAs(aDuck)) {
			System.out.print("She's a Witch!");
			if (apPerson instanceof BurningObject) {
				System.out.println("BUUURNNN!!");
				apPerson.burn();
			}
		}
		else {
			System.out.print("OK, she is not...");
			if (apPerson instanceof BurningObject) {
				System.out.println("Let's burn it anyway!");
				apPerson.burn();
			}
		}

	}
}
