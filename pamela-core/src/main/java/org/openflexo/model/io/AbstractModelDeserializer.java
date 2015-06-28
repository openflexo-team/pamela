/**
 * 
 * Copyright (c) 2015-, Openflexo
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

package org.openflexo.model.io;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.model.ModelEntity;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.DeserializationPolicy;
import org.openflexo.model.factory.ModelFactory;

public abstract class AbstractModelDeserializer implements ModelDeserializer {

	protected ModelFactory modelFactory;
	protected DeserializationPolicy policy;

	/**
	 * Stores already serialized objects where value is the deserialized object and key is an object coding the unique identifier of the
	 * object
	 */
	protected final Map<Object, Object> alreadyDeserializedMap;

	/**
	 * Stored an ordered list of deserialized objects in the order they were instantiated during deserialization phase phase
	 */
	protected final List<DeserializedObject> alreadyDeserialized;

	class DeserializedObject<I> {

		I object;
		ModelEntity<I> modelEntity;

		DeserializedObject(I object, ModelEntity<I> modelEntity) {
			this.object = object;
			this.modelEntity = modelEntity;
		}
	}

	AbstractModelDeserializer(ModelFactory aModelFactory) {
		modelFactory = aModelFactory;
		alreadyDeserializedMap = new HashMap<Object, Object>();
		alreadyDeserialized = new ArrayList<DeserializedObject>();
	}

	/**
	 * 
	 * initialize deserialization for a ModelEntity Object, that is: invoke deserializationInitializer hook defined on the ModelEntity for
	 * any given instance I
	 * 
	 * @param object
	 * @param modelEntity
	 */
	protected <I> void initializeDeserialization(I object, ModelEntity<I> modelEntity) {
		modelFactory.objectIsBeeingDeserialized(object, modelEntity.getImplementedInterface());
		try {
			if (modelEntity.getDeserializationInitializer() != null) {
				if (modelEntity.getDeserializationInitializer().getDeserializationInitializerMethod().getParameterTypes().length == 0) {
					modelEntity.getDeserializationInitializer().getDeserializationInitializerMethod().invoke(object, new Object[0]);
				}
				else if (modelEntity.getDeserializationInitializer().getDeserializationInitializerMethod().getParameterTypes().length == 1) {
					modelEntity.getDeserializationInitializer().getDeserializationInitializerMethod().invoke(object, modelFactory);
				}
				else {
					System.err.println("Wrong number of argument for deserialization initializer "
							+ modelEntity.getDeserializationInitializer().getDeserializationInitializerMethod());
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * finalize deserialization for a ModelEntity Object,that is: invoke serializationInitializer hook defined on the ModelEntity for any
	 * given instance I
	 * 
	 * @param object
	 * @param modelEntity
	 */
	protected <I> void finalizeDeserialization(I object, ModelEntity<I> modelEntity) {
		try {
			if (modelEntity.getDeserializationFinalizer() != null) {
				modelEntity.getDeserializationFinalizer().getDeserializationFinalizerMethod().invoke(object, new Object[0]);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		modelFactory.objectHasBeenDeserialized(object, modelEntity.getImplementedInterface());
	}

	@Override
	public void setDeserializationPolicy(DeserializationPolicy policy) {
		this.policy = policy;

	}

}
