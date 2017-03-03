/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
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
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
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

package org.openflexo.model.io;

import org.openflexo.model.ModelEntity;
import org.openflexo.model.ModelProperty;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.factory.ModelFactory;
import org.xml.sax.SAXException;

/**
 * Represents an object transformed from an XML source with it's meta-informations
 */
public class TransformedObjectInfo {

	private final ModelFactory factory;

	private final Object parent;
	private final ModelProperty<Object> leadingProperty;
	private final ModelEntity<Object> modelEntity;

	private Object object;

	public TransformedObjectInfo(ModelFactory factory, Object parent, ModelProperty<Object> leadingProperty, ModelEntity<Object> modelEntity) {
		this.factory = factory;
		this.parent = parent;
		this.leadingProperty = leadingProperty;
		this.modelEntity = modelEntity;
	}

	public Object getParent() {
		return parent;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public void setFromString(String source) throws SAXException {
		try {
			Class<Object> implementedInterface = modelEntity.getImplementedInterface();
			object = factory.getStringEncoder().fromString(implementedInterface, source);
		} catch (InvalidDataException e) {
			throw new SAXException(e);
		}
	}

	public ModelProperty<Object> getLeadingProperty() {
		return leadingProperty;
	}

	public ModelEntity<Object> getModelEntity() {
		return modelEntity;
	}

	public boolean isConvertible() {
		return factory.getStringEncoder().isConvertable(modelEntity.getImplementedInterface());
	}

}
