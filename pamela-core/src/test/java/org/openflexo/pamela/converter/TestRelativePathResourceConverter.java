package org.openflexo.pamela.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Checks that {@link RelativePathResourceConverter} reads back what it writes: a resource relative to the container resource, and a
 * resource of a jar, which no relative path addresses
 */
public class TestRelativePathResourceConverter {

	/** A resource in a jar of the classpath (jdom2 is a dependency of pamela-core) */
	private static final String JAR_RESOURCE = "org/jdom2/Element.class";

	private static final FileSystemResourceLocatorImpl FILE_SYSTEM = new FileSystemResourceLocatorImpl();

	private static File directory;
	private static Resource directoryResource;

	@BeforeClass
	public static void setUp() throws Exception {
		directory = Files.createTempDirectory("TestRelativePathResourceConverter").toFile();
		directory.deleteOnExit();
		directoryResource = FILE_SYSTEM.retrieveResource(directory);
	}

	@Test
	public void testRelativeFile() throws Exception {
		File imageDirectory = new File(directory, "Images");
		imageDirectory.mkdirs();
		File image = new File(imageDirectory, "image.png");
		Files.write(image.toPath(), new byte[] { 0 });
		image.deleteOnExit();
		imageDirectory.deleteOnExit();

		RelativePathResourceConverter converter = new RelativePathResourceConverter(directoryResource);
		String serialized = converter.convertToString(FILE_SYSTEM.retrieveResource(image));
		assertEquals("Images/image.png", serialized);
		assertEquals(image.getCanonicalFile(), new File(converter.convertFromString(serialized, null).getURI().replace("file:", ""))
				.getCanonicalFile());
	}

	@Test
	public void testJarResource() {
		Resource jarResource = ResourceLocator.locateResource(JAR_RESOURCE);
		assertNotNull(jarResource);
		assertTrue("Expected a resource in a jar: " + jarResource.getURI(), jarResource.getURI().startsWith("jar:"));

		RelativePathResourceConverter converter = new RelativePathResourceConverter(directoryResource);
		String serialized = converter.convertToString(jarResource);
		assertEquals(RelativePathResourceConverter.CLASSPATH_PREFIX + JAR_RESOURCE, serialized);
		Resource read = converter.convertFromString(serialized, null);
		assertNotNull(read);
		assertEquals(jarResource.getURI(), read.getURI());
	}

	@Test
	public void testJarResourceWithoutContainer() {
		Resource jarResource = ResourceLocator.locateResource(JAR_RESOURCE);
		RelativePathResourceConverter converter = new RelativePathResourceConverter(null);
		assertEquals(RelativePathResourceConverter.CLASSPATH_PREFIX + JAR_RESOURCE, converter.convertToString(jarResource));
	}
}
