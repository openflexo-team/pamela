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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.openflexo.pamela.patterns.annotations.OnException;
import org.openflexo.pamela.patterns.annotations.OnException.OnExceptionStategy;

public class PatternExceptionHandler extends PatternStatement<Object> {

	private final OnException annotation;

	public PatternExceptionHandler(PatternDefinition patternDefinition, Method method, OnException annotation) {
		super(patternDefinition, method, annotation.perform());
		this.annotation = annotation;
	}

	@Override
	public Type getExpectedType() {
		return Object.class;
	}

	public OnException getAnnotation() {
		return annotation;
	}

	/**
	 * Return exception triggering this statement
	 * 
	 * @return
	 */
	public Class<? extends Exception> getOnException() {
		return annotation.onException();
	}

	/**
	 * @return semantics to apply when exception is caught
	 */
	public OnExceptionStategy getStrategy() {
		return annotation.strategy();
	}

	@Override
	public String toString() {
		return "PatternExceptionHandler " + getOnException().getSimpleName() + "->" + getExecutionStatementAsString();
	}
}
