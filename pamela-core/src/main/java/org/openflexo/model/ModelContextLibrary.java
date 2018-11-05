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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openflexo.model.exceptions.ModelDefinitionException;

public class ModelContextLibrary {

	private static final Map<Class<?>, ModelContext> contexts = new Hashtable<>();
	private static final Map<Set<Class<?>>, ModelContext> setContexts = new Hashtable<>();

	public static synchronized ModelContext getModelContext(Class<?> baseClass) throws ModelDefinitionException {
		ModelContext context = contexts.get(baseClass);
		if (context == null) {
			contexts.put(baseClass, context = new ModelContext(baseClass));
		}
		return context;
	}

	public static boolean hasContext(Class<?> baseClass) {
		return contexts.get(baseClass) != null;
	}

	public static ModelContext getCompoundModelContext(List<Class<?>> classes) throws ModelDefinitionException {
		if (classes.size() == 1) {
			return getModelContext(classes.get(0));
		}

		Set<Class<?>> set = new HashSet<>(classes);
		ModelContext context = setContexts.get(set);
		if (context == null) {
			setContexts.put(set, context = new ModelContext(classes));
		}
		return context;
	}

	public static ModelContext getCompoundModelContext(Class<?>... classes) throws ModelDefinitionException {
		return getCompoundModelContext(Arrays.asList(classes));
	}

	public static ModelContext getCompoundModelContext(Class<?> baseClass, Class<?>[] classes) throws ModelDefinitionException {
		Class<?>[] newArray = new Class[classes.length + 1];
		for (int i = 0; i < classes.length; i++) {
			newArray[i] = classes[i];
		}
		newArray[classes.length] = baseClass;

		return getCompoundModelContext(newArray);
	}

}
