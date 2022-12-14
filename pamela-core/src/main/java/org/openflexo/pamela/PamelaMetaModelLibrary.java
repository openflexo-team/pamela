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

package org.openflexo.pamela;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * The {@link PamelaMetaModelLibrary} represent the API used to instantiate a PAMELA model (a {@link PamelaMetaModel}). Computed
 * {@link PamelaMetaModel} are stored in an internal cache.<br>
 * 
 * The idea behind this is to instantiate required model using a list of class acting as base entries. The inheritance links as well as the
 * properties links (getter/setter) are explored - both are dependancy links -, as well as <tt>@Imports</tt> and <tt>@Import</tt>
 * annotations, until a closure is computed. That scheme allows to avoid the explicit declaration of a PAMELA model boundary, since this is
 * dynamically performed. This offers a basic solution to model fragmentation.<br>
 * 
 * Note following details regarding model exploration:
 * <ul>
 * <li>If a property link should not be followed (accessed type should not be part of returned {@link PamelaMetaModel}), use
 * <tt>@Getter(...,ignoreType=true)</tt>)</li>
 * <li>If a property type is specialized in PAMELA model to retrieve, declare in that type required entity specializations using
 * <tt>@Imports</tt> annotations in generic type</li>
 * </ul>
 * 
 * @author sylvain
 *
 */
public class PamelaMetaModelLibrary {

	private static final Map<Class<?>, PamelaMetaModel> contexts = new Hashtable<>();
	private static final Map<Set<Class<?>>, PamelaMetaModel> setContexts = new Hashtable<>();

	/**
	 * Return (compute when not existant) a {@link PamelaMetaModel} (a PAMELA model) from supplied base class a unique entry point
	 * 
	 * @param baseClass
	 *            unique entry point
	 * @return
	 * @throws ModelDefinitionException
	 */
	public static synchronized PamelaMetaModel getModelContext(Class<?> baseClass) throws ModelDefinitionException {
		return getModelContext(baseClass, true);
	}

	/**
	 * Return (compute when not existant) a {@link PamelaMetaModel} (a PAMELA model) from supplied base class a unique entry point
	 * 
	 * @param baseClass
	 *            unique entry point
	 * @param isFinalModelContext
	 *            true when final
	 * @return
	 * @throws ModelDefinitionException
	 */
	static synchronized PamelaMetaModel getModelContext(Class<?> baseClass, boolean isFinalModelContext) throws ModelDefinitionException {
		PamelaMetaModel context = contexts.get(baseClass);
		if (context == null) {
			contexts.put(baseClass, context = new PamelaMetaModel(baseClass, isFinalModelContext));
		}
		return context;
	}

	/**
	 * Indicates if {@link PamelaMetaModel} identified by <tt>baseClass</tt> was already computed
	 * 
	 * @param baseClass
	 * @return
	 */
	public static boolean hasContext(Class<?> baseClass) {
		return contexts.get(baseClass) != null;
	}

	/**
	 * Return (compute when not existant) a {@link PamelaMetaModel} (a PAMELA model) from supplied base classes as multiple entry points
	 * 
	 * @param classes
	 *            classes to consider to compute graph closure
	 * @return
	 * @throws ModelDefinitionException
	 */
	public static PamelaMetaModel getCompoundModelContext(List<Class<?>> classes) throws ModelDefinitionException {
		if (classes.size() == 1) {
			return getModelContext(classes.get(0), true);
		}

		Set<Class<?>> set = new HashSet<>(classes);
		PamelaMetaModel context = setContexts.get(set);
		if (context == null) {
			setContexts.put(set, context = new PamelaMetaModel(classes));
		}
		return context;
	}

	/**
	 * Return (compute when not existant) a {@link PamelaMetaModel} (a PAMELA model) from supplied base classes as multiple entry points
	 * 
	 * @param classes
	 *            classes to consider to compute graph closure
	 * @return
	 * @throws ModelDefinitionException
	 */
	public static PamelaMetaModel getCompoundModelContext(Class<?>... classes) throws ModelDefinitionException {
		return getCompoundModelContext(Arrays.asList(classes));
	}

	/**
	 * Return (compute when not existant) a {@link PamelaMetaModel} (a PAMELA model) from supplied base classes as multiple entry points
	 * 
	 * @param baseClass
	 *            main entry point
	 * @param classes
	 *            other classes to consider to compute graph closure
	 * @return
	 * @throws ModelDefinitionException
	 */
	public static PamelaMetaModel getCompoundModelContext(Class<?> baseClass, Class<?>[] classes) throws ModelDefinitionException {
		Class<?>[] newArray = new Class[classes.length + 1];
		for (int i = 0; i < classes.length; i++) {
			newArray[i] = classes[i];
		}
		newArray[classes.length] = baseClass;

		return getCompoundModelContext(newArray);
	}

	/**
	 * Clear cache
	 */
	public static void clearCache() {
		contexts.clear();
		setContexts.clear();
	}
}
