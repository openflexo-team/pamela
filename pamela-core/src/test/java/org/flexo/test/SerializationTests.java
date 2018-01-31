package org.flexo.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.flexo.model.AbstractNode;
import org.flexo.model.FlexoProcess;
import org.flexo.model.MyNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openflexo.model.AbstractPAMELATest;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.RestrictiveDeserializationException;
import org.openflexo.model.exceptions.RestrictiveSerializationException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.factory.SerializationPolicy;
import org.openflexo.toolbox.Duration;
import org.openflexo.toolbox.Duration.DurationUnit;
import org.openflexo.toolbox.FileFormat;

public class SerializationTests extends AbstractPAMELATest {

	private static final String NODE_NAME = "aNode";
	private static final String PROPERTY_VALUE = "aValue";
	private static final Duration DURATION_VALUE = new Duration(10, DurationUnit.SECONDS);
	private static final FileFormat FILEFORMAT_VALUE = FileFormat.JAR;
	private File file;
	private ModelFactory factory;
	private ModelFactory factory2;

	@Override
	@Before
	public void setUp() throws IOException, ModelDefinitionException {
		clearModelEntityLibrary();
		file = File.createTempFile("PAMELA-TestSerialization", ".xml");
		factory2 = new ModelFactory(MyNode.class);
		factory = new ModelFactory(FlexoProcess.class);
	}

	@Override
	public void tearDown() {
		file.delete();
	}

	@Test
	public void testSerializationPolicy() throws Exception {

		Assert.assertNull(factory.getModelContext().getModelEntity(MyNode.class));
		Assert.assertNotNull(factory2.getModelContext().getModelEntity(MyNode.class));
		validateBasicModelContext(factory.getModelContext());
		validateBasicModelContext(factory2.getModelContext());
		FlexoProcess process = (FlexoProcess) factory.newInstance(FlexoProcess.class).init();
		MyNode node = (MyNode) factory2.newInstance(MyNode.class).init(NODE_NAME);
		node.setMyProperty(PROPERTY_VALUE);
		node.setMyDuration(DURATION_VALUE);
		node.setMyFileformat(FILEFORMAT_VALUE);
		node.setMyLevel(Level.ALL);
		process.addToNodes(node);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(process, fos, SerializationPolicy.RESTRICTIVE, true);
			Assert.fail("Restrictive serialization should not allow the serialization of a " + MyNode.class.getName());
		} catch (RestrictiveSerializationException e) {
			// Yes this is what we wanted
		} catch (Exception e) {
			fail(e.getMessage());
		}
		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(process, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (RestrictiveSerializationException e) {
			Assert.fail("Extensive serialization should allow the serialization of a " + MyNode.class.getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		// Just to make sure
		process = null;
		factory = new ModelFactory(FlexoProcess.class);
		try (FileInputStream fis = new FileInputStream(file)) {
			process = (FlexoProcess) factory.deserialize(fis, DeserializationPolicy.RESTRICTIVE);
			Assert.fail("Restrictive deserialization should not allow the deserialization of a " + MyNode.class.getName());
		} catch (RestrictiveDeserializationException e) {
			// Yes this is what we wanted
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			process = (FlexoProcess) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (RestrictiveDeserializationException e) {
			Assert.fail("Extensive deserialization should allow the deserialization of a " + MyNode.class.getName());
		}
		Assert.assertEquals(1, process.getNodes().size());
		AbstractNode aNode = process.getNodeNamed(NODE_NAME);
		Assert.assertTrue(aNode instanceof MyNode);
		Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());

		// The next block is necessary to ensure that serialization remains consistent when using EXTENSIVE policy (ie, class names and
		// stuffs remain serialized)
		try (FileOutputStream fos = new FileOutputStream(file)) {
			factory.serialize(process, fos, SerializationPolicy.EXTENSIVE, true);
		} catch (RestrictiveSerializationException e) {
			Assert.fail("Extensive serialization should allow the serialization of a " + MyNode.class.getName());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		factory = new ModelFactory(FlexoProcess.class);
		try (FileInputStream fis = new FileInputStream(file)) {
			process = (FlexoProcess) factory.deserialize(fis, DeserializationPolicy.EXTENSIVE);
		} catch (RestrictiveDeserializationException e) {
			Assert.fail("Extensive deserialization should allow the deserialization of a " + MyNode.class.getName());
		}
		Assert.assertEquals(1, process.getNodes().size());
		aNode = process.getNodeNamed(NODE_NAME);
		Assert.assertTrue(aNode instanceof MyNode);
		Assert.assertEquals(PROPERTY_VALUE, ((MyNode) aNode).getMyProperty());
	}
}
