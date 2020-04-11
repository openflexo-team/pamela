/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-core, a component of the software infrastructure 
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

package org.openflexo.pamela.patterns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * This abstract class represents a pattern. Its instances are to be wrapped in the patternContext.<br>
 * It has the responsibility of:
 * <ul>
 * <li>Handling method call before and after invoke to perform execution or check relevant to the pattern.</li>
 * <li>Relaying the instance discovery to the relevant component of the pattern.</li>
 * </ul>
 *
 * @author C. SILVA
 */
@Deprecated
public abstract class AbstractPattern {
	protected final PatternContext context;
	protected final String id;

	/**
	 * Constructor of the class.
	 * 
	 * @param context
	 *            Reference to the {@link PatternContext} instance
	 * @param id
	 *            Unique identifier of the pattern
	 */
	public AbstractPattern(PatternContext context, String id) {
		this.context = context;
		this.id = id;
	}

	/**
	 * Method called before every method invoke.
	 * 
	 * @param self
	 *            Object on which the method is called
	 * @param method
	 *            method to be invoked
	 * @param klass
	 *            Class of the class tree of <code>self</code> involved in the pattern
	 * @param args
	 *            Argument passed to the to-be-called method
	 * @return {@link ReturnWrapper} wrapping the return value of the method processing, if relevant, and a boolean stating whether the
	 *         execution of the method should be continued after the call of this method or not.
	 * @throws InvocationTargetException
	 *             in case of error during internal calls
	 * @throws IllegalAccessException
	 *             in case of error during internal calls
	 * @throws NoSuchMethodException
	 *             in case of error during internal calls
	 */
	public abstract ReturnWrapper processMethodBeforeInvoke(Object self, Method method, Class klass, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

	/**
	 * Method called when a new pattern-related instance is discovered by the {@link PatternContext}
	 * 
	 * @param instance
	 *            instance to discover
	 * @param klass
	 *            Class of the class tree of <code>self</code> involved in the pattern
	 */
	public abstract void discoverInstance(Object instance, Class klass);

	/**
	 * Method called after every method invoke
	 * 
	 * @param self
	 *            Object on which the method is called
	 * @param method
	 *            Just-invoked method
	 * @param klass
	 *            Class of the class tree of <code>self</code> involved in the pattern
	 * @param returnValue
	 *            Returned Value of the just-invoked method
	 * @param args
	 *            Argument passed to the just-invoked methods
	 */
	public abstract void processMethodAfterInvoke(Object self, Method method, Class klass, Object returnValue, Object[] args);

	/**
	 * Identifies the entity type to instantiate with the given class, and if relevant, instantiate the associated entity.
	 * 
	 * @param baseClass
	 *            Class to analyze
	 * @throws ModelDefinitionException
	 *             In case of error during the class analysis.
	 */
	public abstract void attachClass(Class baseClass) throws ModelDefinitionException;

	/**
	 * @return the {@link PatternContext} wrapping this pattern
	 */
	public PatternContext getContext() {
		return this.context;
	}

	/**
	 * @return the unique identifier of this pattern
	 */
	public String getID() {
		return this.id;
	}
}
