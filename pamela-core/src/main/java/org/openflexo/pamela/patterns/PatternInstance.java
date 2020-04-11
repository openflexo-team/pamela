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
import java.net.Authenticator;

import org.openflexo.pamela.ModelContext;

/**
 * Abstract base class for an instance of a {@link PatternDefinition}
 * 
 * It has the responsibility of:
 * <ul>
 * <li>Maintaining state variables of the pattern instance</li>
 * <li>Enforcing invariants of the {@link Authenticator} annotated class.</li>
 * <li>Enforcing preconditions of the {@link Authenticator} annotated class.</li>
 * <li>Enforcing postconditions of the {@link Authenticator} annotated class.</li>
 * </ul>
 *
 * @author Caine Silva, Sylvain Guerin
 *
 * @param <P>
 *            Type of {@link PatternDefinition} this instance is an instance of
 */
public abstract class PatternInstance<P extends PatternDefinition> {

	private P patternDefinition;

	public PatternInstance(P patternDefinition) {
		this.patternDefinition = patternDefinition;
		patternDefinition.getModelContext().registerPatternInstance(this);
	}

	public P getPatternDefinition() {
		return patternDefinition;
	}

	public ModelContext getModelContext() {
		return patternDefinition.getModelContext();
	}

	protected void registerStakeHolder(Object stakeHolder, String role) {
		patternDefinition.getModelContext().registerStakeHolderForPatternInstance(stakeHolder, role, this);
	}

	public abstract ReturnWrapper processMethodBeforeInvoke(Object instance, Method method, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

	public abstract void processMethodAfterInvoke(Object instance, Method method, Object returnValue, Object[] args)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

}
