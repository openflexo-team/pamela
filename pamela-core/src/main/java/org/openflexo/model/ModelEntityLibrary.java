/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openflexo.model.exceptions.ModelDefinitionException;

public class ModelEntityLibrary {

	private static Map<Class<?>, ModelEntity<?>> entities = new Hashtable<>();

	private static List<ModelEntity<?>> newEntities = new ArrayList<>();

	static synchronized <I> ModelEntity<I> importEntity(Class<I> implementedInterface) throws ModelDefinitionException {
		ModelEntity<I> modelEntity = (ModelEntity<I>) entities.get(implementedInterface);
		if (modelEntity == null) {
			modelEntity = get(implementedInterface, true);
			for (ModelEntity<?> e : newEntities) {
				e.mergeProperties();
			}
			newEntities.clear();
		}
		return modelEntity;
	}

	static <I> ModelEntity<I> get(Class<I> implementedInterface, boolean create) throws ModelDefinitionException {
		ModelEntity<I> modelEntity = (ModelEntity<I>) entities.get(implementedInterface);
		if (modelEntity == null && create) {
			if (!ModelEntity.isModelEntity(implementedInterface)) {
				throw new ModelDefinitionException("Class " + implementedInterface + " is not a ModelEntity.");
			}
			synchronized (ModelEntityLibrary.class) {
				entities.put(implementedInterface, modelEntity = new ModelEntity<>(implementedInterface));
				modelEntity.init();
				newEntities.add(modelEntity);
			}
		}
		return modelEntity;
	}

	static <I> ModelEntity<I> get(Class<I> implementedInterface) {
		try {
			return get(implementedInterface, false);
		} catch (ModelDefinitionException e) {
			// Never happens
			return null;
		}
	}

	static boolean has(Class<?> implementedInterface) {
		return entities.containsKey(implementedInterface);
	}

	/**
	 * For testings purposes only.
	 */
	public static void clear() {
		entities.clear();
	}
}
