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

	private final boolean convertible;
	private final Object parent;
	private final Object object;
	private final ModelProperty<Object> leadingProperty;
	private final ModelEntity<Object> modelEntity;

	public TransformedObjectInfo(Object object, ModelProperty<Object> leadingProperty, Object parent, ModelEntity<Object> modelEntity) {
		this(false, object, leadingProperty, parent, modelEntity);
	}

	public TransformedObjectInfo(ModelProperty<Object> leadingProperty, Object parent, ModelEntity<Object> modelEntity) {
		this(true, null, leadingProperty, parent, modelEntity);
	}

	private TransformedObjectInfo(boolean convertible, Object object, ModelProperty<Object> leadingProperty, Object parent, ModelEntity<Object> modelEntity) {
		this.convertible = convertible;
		this.parent = parent;
		this.object = object;
		this.leadingProperty = leadingProperty;
		this.modelEntity = modelEntity;
	}

	public boolean isConvertible() {
		return convertible;
	}

	public Object getParent() {
		return parent;
	}

	public Object getObject() {
		return object;
	}

	public ModelProperty<Object> getLeadingProperty() {
		return leadingProperty;
	}

	public ModelEntity<Object> getModelEntity() {
		return modelEntity;
	}

	public TransformedObjectInfo convert(ModelFactory factory, String source) throws SAXException {
		try {
			Class<Object> implementedInterface = modelEntity.getImplementedInterface();
			Object object = factory.getStringEncoder().fromString(implementedInterface, source);
			return new TransformedObjectInfo(object, leadingProperty, parent, modelEntity);
		} catch (InvalidDataException e) {
			throw new SAXException(e);
		}
	}
}
