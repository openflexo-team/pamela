/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.model.io.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.model.ModelEntityLibrary;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.io.JSonModelDeserializer;
import org.openflexo.model4.Node;
import org.openflexo.model4.Node.NodeImpl;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

@RunWith(OrderedRunner.class)
public class BasicJSonDeserializeTest {

	private static File file;
	private static ModelFactory factory;
	private static JSonModelDeserializer deserializer;

	@BeforeClass
	public static void setUpClass() throws IOException, ModelDefinitionException {
		ModelEntityLibrary.clear();
		file = ((FileResourceImpl) ResourceLocator.locateResource("json/SampleNodes.json")).getFile();
		factory = new ModelFactory(Node.class);
		deserializer = new JSonModelDeserializer(factory);
		factory.setModelDeserializer(deserializer);
	}

	@AfterClass
	public static void tearDownClass() {
		// Nothing to Do
	}

	@Test
	@TestOrder(2)
	public void testDeserialize() {

		FileInputStream fis = null;
		Node rootNode = null;

		try {
			fis = new FileInputStream(file);
			rootNode = (Node) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fis);
		}

		assertNotNull(rootNode);

		System.out.println(NodeImpl.DESERIALIZATION_TRACE);

		assertEquals(" BEGIN:First Root Node BEGIN:S BEGIN:T BEGIN:TT END:First Root Node END:S END:T END:TT",
				NodeImpl.DESERIALIZATION_TRACE);

	}

}
