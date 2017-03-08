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

import java.lang.reflect.Method;
import org.openflexo.model.DeserializationFinalizer;
import org.openflexo.model.DeserializationInitializer;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.ModelProperty;
import org.openflexo.model.exceptions.InvalidDataException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.xml.sax.SAXException;

/**
 * Represents an object transformed from an XML source with it's meta-informations
 */
public class TransformedObjectInfo {

	private static final Object[] EMPTY_ARGS = new Object[0];

	private final ModelFactory factory;

	private final Object parent;
	private final ModelProperty<Object> leadingProperty;
	private final ModelEntity<Object> modelEntity;
	private final Class<Object> implementedInterface;

	private boolean resolved = false;
	private Object object;

	public TransformedObjectInfo(
			ModelFactory factory, Object parent, ModelProperty<Object> leadingProperty, ModelEntity<Object> modelEntity
	) {
		this.factory = factory;
		this.parent = parent;
		this.leadingProperty = leadingProperty;
		if (modelEntity != null) {
			this.modelEntity = modelEntity;
			this.implementedInterface = modelEntity.getImplementedInterface();
		}
		else {
			this.implementedInterface = (Class<Object>) leadingProperty.getType();
			this.modelEntity = factory.getModelContext().getModelEntity(implementedInterface);
		}
	}

	public Object getParent() {
		return parent;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
		this.resolved = true;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setFromString(String source) throws SAXException {
		try {
			setObject(factory.getStringEncoder().fromString(implementedInterface, source));
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
		return factory.getStringEncoder().isConvertable(implementedInterface);
	}

	public void initializeDeserialization() throws SAXException {
		if (object != null) {
			factory.objectIsBeeingDeserialized(object, implementedInterface);

			if (modelEntity != null) {
				factory.getHandler(object).setDeserializing(true);

				try {
					DeserializationInitializer deserializationInitializer = modelEntity.getDeserializationInitializer();
					if (deserializationInitializer != null) {
						Method deserializationInitializerMethod = deserializationInitializer.getDeserializationInitializerMethod();
						if (deserializationInitializerMethod.getParameterTypes().length == 0) {
							deserializationInitializerMethod.invoke(object, EMPTY_ARGS);
						}
						else if (deserializationInitializerMethod.getParameterTypes().length == 1) {
							deserializationInitializerMethod.invoke(object, factory);
						}
						else {
							String message = "Wrong number of argument for deserialization initializer " + deserializationInitializerMethod;
							throw new ModelDefinitionException(message);
						}
					}
				} catch (Exception e) {
					throw new SAXException(e);
				}
			}
		}
	}

	public void finalizeDeserialization() throws SAXException {
		if (modelEntity != null && object != null) {
			// closes status
			factory.getHandler(object).setDeserializing(false);

			// calls ended
			try {
				DeserializationFinalizer deserializationFinalizer = modelEntity.getDeserializationFinalizer();
				if (deserializationFinalizer != null) {
					deserializationFinalizer.getDeserializationFinalizerMethod().invoke(object, EMPTY_ARGS);
				}
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}
		factory.objectHasBeenDeserialized(object, implementedInterface);
	}

}
