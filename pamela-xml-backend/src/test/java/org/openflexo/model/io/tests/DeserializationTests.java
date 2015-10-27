package org.openflexo.model.io.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jdom2.JDOMException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.model.ModelEntityLibrary;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.factory.SerializationPolicy;
import org.openflexo.model4.Node;
import org.openflexo.model4.Node.NodeImpl;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

@RunWith(OrderedRunner.class)
public class DeserializationTests {

	private static File file;
	private static ModelFactory factory;

	@BeforeClass
	public static void setUpClass() throws IOException, ModelDefinitionException {
		ModelEntityLibrary.clear();
		file = File.createTempFile("PAMELA-TestDeserialization", ".xml");
		factory = new ModelFactory(Node.class);
	}

	@AfterClass
	public static void tearDownClass() {
		file.delete();
	}

	@Test
	@TestOrder(1)
	public void testInitializeAModel() throws IOException, ModelDefinitionException, JDOMException, InvalidDataException {

		Assert.assertNotNull(factory.getModelContext().getModelEntity(Node.class));

		Node rootNode = factory.newInstance(Node.class);
		rootNode.setName("Root");
		Node childNode1 = factory.newInstance(Node.class);
		childNode1.setName("Node1");
		rootNode.addToNodes(childNode1);
		Node childNode2 = factory.newInstance(Node.class);
		childNode2.setName("Node2");
		rootNode.addToNodes(childNode2);
		Node childNode3 = factory.newInstance(Node.class);
		childNode3.setName("Node3");
		rootNode.addToNodes(childNode3);
		Node childNode21 = factory.newInstance(Node.class);
		childNode21.setName("Node21");
		childNode2.addToNodes(childNode21);
		Node childNode22 = factory.newInstance(Node.class);
		childNode22.setName("Node22");
		childNode2.addToNodes(childNode22);
		Node childNode23 = factory.newInstance(Node.class);
		childNode23.setName("Node23");
		childNode2.addToNodes(childNode23);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			factory.serialize(rootNode, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fos);
		}

		System.out.println(factory.stringRepresentation(rootNode));
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
			Assert.fail(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fis);
		}

		assertNotNull(rootNode);

		System.out.println(NodeImpl.DESERIALIZATION_TRACE);

		assertEquals(
				" BEGIN:Root BEGIN:Node1 BEGIN:Node2 BEGIN:Node21 BEGIN:Node22 BEGIN:Node23 BEGIN:Node3 END:Root END:Node1 END:Node2 END:Node21 END:Node22 END:Node23 END:Node3",
				NodeImpl.DESERIALIZATION_TRACE);

	}
}
