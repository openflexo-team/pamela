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

import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;
import org.openflexo.rm.Resource;

/**
 * A converter that allows to address a {@link Resource} relatively to another {@link Resource} (the container resource)<br>
 * 
 * This converter also manage an alternative container resource
 * 
 * 
 * @author sylvain
 *
 */
public class RelativePathResourceConverter extends Converter<Resource> {

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
	public Resource convertFromString(String value, ModelFactory factory) {

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
