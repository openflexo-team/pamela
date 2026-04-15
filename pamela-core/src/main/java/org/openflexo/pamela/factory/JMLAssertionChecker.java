/**
 *
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 *
 * This file is part of Pamela-core, a component of the software infrastructure
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

package org.openflexo.pamela.factory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.openflexo.pamela.jml.JMLEnsures;
import org.openflexo.pamela.jml.JMLMethodDefinition;
import org.openflexo.pamela.jml.JMLRequires;

/**
 * Handles JML (Java Modeling Language) contract checking for a PAMELA object.<br>
 * Manages pre-conditions (@Requires), post-conditions (@Ensures), and invariants (@Invariant).<br>
 * Extracted from {@link ProxyMethodHandler} to isolate assertion-checking concerns.
 *
 * @author sylvain
 * @param <I> type of object managed by the parent {@link ProxyMethodHandler}
 */
public class JMLAssertionChecker<I> {

	private final ProxyMethodHandler<I> handler;

	private boolean enabled = false;
	private final Stack<Method> assertionCheckingStack = new Stack<>();
	private final Map<Method, Map<String, Object>> historyValues = new HashMap<>();

	public JMLAssertionChecker(ProxyMethodHandler<I> handler) {
		this.handler = handler;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable() {
		this.enabled = true;
	}

	public void disable() {
		this.enabled = false;
	}

	public Stack<Method> getAssertionCheckingStack() {
		return assertionCheckingStack;
	}

	/**
	 * Called before method invocation: checks invariant and pre-condition (@Requires), captures history values for post-condition.
	 *
	 * @return true if assertion checking was actually performed (false if already on the stack, to avoid recursion)
	 */
	boolean checkOnEntry(Method method, Object[] args) {
		if (!assertionCheckingStack.isEmpty() && assertionCheckingStack.peek() == method) {
			return false;
		}
		assertionCheckingStack.push(method);
		checkInvariant();

		JMLMethodDefinition jmlMethodDefinition = handler.getModelEntity().getJMLMethodDefinition(method);
		if (jmlMethodDefinition != null) {
			if (jmlMethodDefinition.getRequires() != null) {
				((JMLRequires) jmlMethodDefinition.getRequires()).check(handler, args);
			}
			if (jmlMethodDefinition.getEnsures() != null) {
				Map<String, Object> historyValuesForThisMethod = ((JMLEnsures) jmlMethodDefinition.getEnsures())
						.checkOnEntry(handler, args);
				historyValues.put(method, historyValuesForThisMethod);
			}
		}
		return true;
	}

	/**
	 * Called after method invocation: checks invariant and post-condition (@Ensures).
	 */
	void checkOnExit(Method method, Object[] args) {
		checkInvariant();
		JMLMethodDefinition jmlMethodDefinition = handler.getModelEntity().getJMLMethodDefinition(method);
		if (jmlMethodDefinition != null) {
			if (jmlMethodDefinition.getEnsures() != null) {
				((JMLEnsures) jmlMethodDefinition.getEnsures()).checkOnExit(handler, args, historyValues.get(method));
			}
		}
		assertionCheckingStack.pop();
	}

	@SuppressWarnings("unchecked")
	private void checkInvariant() {
		if (handler.getModelEntity().getInvariant() != null) {
			((org.openflexo.pamela.jml.JMLInvariant) handler.getModelEntity().getInvariant()).check(handler);
		}
	}
}
