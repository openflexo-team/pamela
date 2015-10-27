package org.openflexo.model.io.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openflexo.model.AbstractNode;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.model.FlexoProcess;
import org.openflexo.model.MyNode;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.RestrictiveDeserializationException;
import org.openflexo.model.exceptions.RestrictiveSerializationException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.factory.SerializationPolicy;
import org.openflexo.model.io.JSonModelSerializer;

public class BasicJSonSerializationTests extends AbstractPAMELATest {

	private static final String NODE_NAME = "aNode";
	private static final String PROPERTY_VALUE = "aValue";
	private File file;
	private ModelFactory factory;
	private ModelFactory factory2;
	private JSonModelSerializer modelSerializer2;
	private JSonModelSerializer modelSerializer;

	@Override
	@Before
	public void setUp() throws IOException, ModelDefinitionException {
		clearModelEntityLibrary();
		file = File.createTempFile("PAMELA-TestSerialization", ".xml");

		factory2 = new ModelFactory(MyNode.class);
		modelSerializer2 = new JSonModelSerializer(factory2);
		factory2.setModelSerializer(modelSerializer2);

		factory = new ModelFactory(FlexoProcess.class);
		modelSerializer = new JSonModelSerializer(factory);
		factory.setModelSerializer(modelSerializer);
	}

	@Override
	public void tearDown() {
		file.delete();
	}

	@Test
	public void testSerializationPolicy() throws IOException, ModelDefinitionException, JDOMException, InvalidDataException {

		Assert.assertNull(factory.getModelContext().getModelEntity(MyNode.class));
		Assert.assertNotNull(factory2.getModelContext().getModelEntity(MyNode.class));
		validateBasicModelContext(factory.getModelContext());
		validateBasicModelContext(factory2.getModelContext());
		FlexoProcess process = (FlexoProcess) factory.newInstance(FlexoProcess.class).init();
		MyNode node = (MyNode) factory2.newInstance(MyNode.class).init(NODE_NAME);
		node.setMyProperty(PROPERTY_VALUE);
		process.addToNodes(node);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			factory.serialize(process, fos, SerializationPolicy.RESTRICTIVE, true);
			Assert.fail("Restrictive serialization should not allow the serialization of a " + MyNode.class.getName());
		} catch (RestrictiveSerializationException e) {
			// Yes this is what we wanted
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fos);
		}
		try {
			fos = new FileOutputStream(file);
			factory.serialize(process, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (RestrictiveSerializationException e) {
			Assert.fail("Extensive serialization should allow the serialization of a " + MyNode.class.getName());
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fos);
		}
		// Just to make sure
		process = null;
		factory = new ModelFactory(FlexoProcess.class);
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(file);
			process = (FlexoProcess) factory.deserialize(fis, DeserializationPolicy.RESTRICTIVE);
			Assert.fail("Restrictive deserialization should not allow the deserialization of a " + MyNode.class.getName());
		} catch (RestrictiveDeserializationException e) {
			// Yes this is what we wanted
		} finally {
			IOUtils.closeQuietly(fis);
		}

		try {
			fis = new FileInputStream(file);
			process = (FlexoProcess) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (RestrictiveDeserializationException e) {
			Assert.fail("Extensive deserialization should allow the deserialization of a " + MyNode.class.getName());
		} finally {
			IOUtils.closeQuietly(fis);
		}
		Assert.assertEquals(1, process.getNodes().size());
		AbstractNode aNode = process.getNodeNamed(NODE_NAME);
		Assert.assertTrue(aNode instanceof MyNode);
		Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());

		// The next block is necessary to ensure that serialization remains consistent when using EXTENSIVE policy (ie, class names and
		// stuffs remain serialized)
		try {
			fos = new FileOutputStream(file);
			factory.serialize(process, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (RestrictiveSerializationException e) {
			Assert.fail("Extensive serialization should allow the serialization of a " + MyNode.class.getName());
		} catch (Exception e) {
			fail(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fos);
		}
		factory = new ModelFactory(FlexoProcess.class);
		try {
			fis = new FileInputStream(file);
			process = (FlexoProcess) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (RestrictiveDeserializationException e) {
			Assert.fail("Extensive deserialization should allow the deserialization of a " + MyNode.class.getName());
		} finally {
			IOUtils.closeQuietly(fis);
		}
		Assert.assertEquals(1, process.getNodes().size());
		aNode = process.getNodeNamed(NODE_NAME);
		Assert.assertTrue(aNode instanceof MyNode);
		Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());
	}
}
