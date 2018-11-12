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

import org.openflexo.pamela.StringConverterLibrary.Converter;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.rm.Resource;

/**
 * A converter that allows to address a {@link Resource} relatively to another {@link Resource} (the container resource)
 * 
 * @author sylvain
 *
 */
public class RelativePathResourceConverter extends Converter<Resource> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(RelativePathResourceConverter.class.getPackage().getName());

	private Resource containerResource;

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

	@Override
	public Resource convertFromString(String value, ModelFactory factory) {

		// System.out.println("Je cherche la resource " + value + " depuis " + containerResource);

		Resource resourceloc = containerResource.locateResource(value);

		// System.out.println("Je trouve " + resourceloc);

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("********* convertFromString " + value + " return " + resourceloc.toString());
		}
		return resourceloc;
	}

	@Override
	public String convertToString(Resource value) {
		if (containerResource == null) {
			logger.warning("Could not compute relative path of " + value + " with RelativePathConverter bound to containerResource=null");
			return null;
		}
		return containerResource.computeRelativePath(value);
	}

}
