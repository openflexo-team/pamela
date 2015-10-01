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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.model.ModelContext;
import org.openflexo.model.factory.ModelFactory;

import witchmodel.FloatingStuff;
import witchmodel.Person;
import witchmodel.PhysicalObject;

public class WitchBasicTests extends AbstractPAMELATest {

	private ModelFactory factory;
	private ModelContext modelContext;

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
		modelContext = new ModelContext(PhysicalObject.class, Person.class);
		factory = new ModelFactory(modelContext);
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

		System.out.println(modelContext.debug());

		assertEquals(3, modelContext.getEntityCount());

	}

	public void test2() throws Exception {

		PhysicalObject apObject = factory.newInstance(PhysicalObject.class);
		assertTrue(apObject instanceof PhysicalObject);

		apObject.setDensity((float) 2.25);
		apObject.setWidth((float) 1);
		apObject.setHeight((float) 0.5);
		apObject.setLength((float) 0.25);

		System.out.println("Volume: " + apObject.getFullVolume() + " m3");
		System.out.println("Weight: " + apObject.getWeight() + " Kg");

		assertEquals((float) 0.125, apObject.getFullVolume().floatValue());
		assertEquals((float) (0.125 * 2.25 * 1000), apObject.getWeight().floatValue());

		try {
			FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml");
			factory.serialize(apObject, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

	public void testWitch() throws Exception {

		Person apObject = factory.newInstance(Person.class);
		assertTrue(apObject instanceof Person);

		apObject.setDensity((float) 1);
		apObject.setWidth((float) 0.3);
		apObject.setHeight((float) 1.75);
		apObject.setLength((float) 0.15);

		System.out.println("Volume: " + apObject.getFullVolume() + " m3");
		System.out.println("Weight: " + apObject.getWeight() + " Kg");
		System.out.println("Sous l'eau de: " + ((FloatingStuff) apObject).getInWaterDepth() + " m");

		try {
			FileOutputStream fos = new FileOutputStream("/tmp/TestFile.xml");
			factory.serialize(apObject, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}
}
