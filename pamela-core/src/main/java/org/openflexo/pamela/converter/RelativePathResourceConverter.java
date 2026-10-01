/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.pamela.converter;

import java.util.logging.Level;

import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.rm.ClasspathResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * A converter that allows to address a {@link Resource} relatively to another {@link Resource} (the container resource)<br>
 * 
 * This converter also manage an alternative container resource<br>
 * 
 * A resource of the classpath which cannot be addressed relatively to the container resources (typically a resource in a jar, such as
 * an image shipped with a library) is addressed by its classpath path, prefixed with {@value #CLASSPATH_PREFIX}
 * 
 * 
 * @author sylvain
 *
 */
public class RelativePathResourceConverter extends Converter<Resource> {

	/** Prefix of a resource addressed by its path in the classpath */
	public static final String CLASSPATH_PREFIX = "classpath:";

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(RelativePathResourceConverter.class.getPackage().getName());

	private Resource containerResource;
	private Resource alternativeContainerResource;

	public RelativePathResourceConverter(Resource containerResource) {
		super(Resource.class);
		this.containerResource = containerResource;
	}

	public Resource getContainerResource() {
		return containerResource;
	}

	public void setContainerResource(Resource containerResource) {
		this.containerResource = containerResource;
	}

	public Resource getAlternativeContainerResource() {
		return alternativeContainerResource;
	}

	public void setAlternativeContainerResource(Resource alternativeContainerResource) {
		this.alternativeContainerResource = alternativeContainerResource;
	}

	@Override
	public Resource convertFromString(String value, PamelaModelFactory factory) {

		if (value != null && value.startsWith(CLASSPATH_PREFIX)) {
			return ResourceLocator.locateResource(value.substring(CLASSPATH_PREFIX.length()));
		}

		// System.out.println("Je cherche " + value);

		Resource resourceloc = containerResource.locateResource(value);

		// System.out.println("Je trouve " + resourceloc + " containerResource=" + containerResource);

		if (resourceloc == null && alternativeContainerResource != null) {
			resourceloc = alternativeContainerResource.locateResource(value);
		}

		// System.out.println("Je trouve " + resourceloc + " alternativeContainerResource=" + alternativeContainerResource);

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("********* convertFromString " + value + " return " + resourceloc.toString());
		}
		return resourceloc;
	}

	@Override
	public String convertToString(Resource value) {
		// No relative path addresses a resource of a jar from container resources out of a jar: use its classpath path. Without
		// container resource, there is no relative path at all
		boolean fromOutsideJar = isInJar(value) && !isInJar(containerResource) && !isInJar(alternativeContainerResource);
		if (fromOutsideJar || containerResource == null && alternativeContainerResource == null) {
			String classpathPath = getClasspathPath(value);
			if (classpathPath != null) {
				return CLASSPATH_PREFIX + classpathPath;
			}
		}
		String relativePath = computeRelativePath(value);
		if (!locatesBack(relativePath, value)) {
			// No relative path to the container resources addresses this resource: use its classpath path, if any
			String classpathPath = getClasspathPath(value);
			if (classpathPath != null) {
				return CLASSPATH_PREFIX + classpathPath;
			}
		}
		return relativePath;
	}

	private static boolean isInJar(Resource resource) {
		return resource != null && resource.getURI() != null && resource.getURI().startsWith("jar:");
	}

	/**
	 * Tells if supplied path, relative to the container resources, locates supplied resource
	 */
	private boolean locatesBack(String relativePath, Resource value) {
		if (relativePath == null || value == null) {
			return false;
		}
		Resource located = containerResource != null ? containerResource.locateResource(relativePath) : null;
		if (located == null && alternativeContainerResource != null) {
			located = alternativeContainerResource.locateResource(relativePath);
		}
		return located != null && located.getURI() != null && located.getURI().equals(value.getURI());
	}

	/**
	 * Return the path of supplied resource in the classpath, when it is a resource of the classpath and this path locates it
	 */
	private static String getClasspathPath(Resource value) {
		if (value == null || !(value.getLocator() instanceof ClasspathResourceLocatorImpl)) {
			return null;
		}
		String path = value.getRelativePath();
		// A resource of a classpath directory may hold an absolute path
		Resource located = value.getLocator().locateResource(path);
		if (located != null && located.getURI() != null && located.getURI().equals(value.getURI())) {
			return path;
		}
		return null;
	}

	private String computeRelativePath(Resource value) {

		// System.out.println("Je cherche a encoder la resource " + value + " depuis " + containerResource);
		// System.out.println("Je peux aussi essayer avec " + value + " depuis " + alternativeContainerResource);

		/*if (value instanceof FileResourceImpl) {
			if (containerResource != null) {
				System.out.println(containerResource.computeRelativePath(value));
				System.out.println("distance: "
						+ FileUtils.distance(((FileResourceImpl) value).getFile(), ((FileResourceImpl) containerResource).getFile()));
			}
			if (alternativeContainerResource != null) {
				System.out.println(alternativeContainerResource.computeRelativePath(value));
				System.out.println("distance: " + FileUtils.distance(((FileResourceImpl) value).getFile(),
						((FileResourceImpl) alternativeContainerResource).getFile()));
			}
		}*/

		if (containerResource == null) {
			if (alternativeContainerResource != null) {
				return alternativeContainerResource.computeRelativePath(value);
			}
			logger.warning("Could not compute relative path of " + value
					+ " with RelativePathConverter bound to containerResource=null and alternativeContainerResource=null");
			return null;
		}
		else {
			if (alternativeContainerResource != null) {
				int d1 = containerResource.distance(value);
				int d2 = alternativeContainerResource.distance(value);
				if (d1 < d2) {
					// System.out.println("Du coup on retourne " + containerResource.computeRelativePath(value));
					return containerResource.computeRelativePath(value);
				}
				else {
					// System.out.println("Du coup2 on retourne " + alternativeContainerResource.computeRelativePath(value));
					return alternativeContainerResource.computeRelativePath(value);
				}
			}
			return containerResource.computeRelativePath(value);
		}

	}

}
