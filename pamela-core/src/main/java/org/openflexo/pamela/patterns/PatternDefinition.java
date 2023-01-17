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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.java.JavaBindingFactory;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.PamelaUtils;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.annotations.Ensures;
import org.openflexo.pamela.patterns.annotations.OnException;
import org.openflexo.pamela.patterns.annotations.Requires;

/**
 * Abstract base class for an occurence of an <code>Pattern</code>.<br>
 * 
 * An instance is uniquely identified by the <code>patternID</code> field of associated annotations.<br>
 * 
 * It has the responsibility of:
 * <ul>
 * <li>Managing life-cycle of related {@link PatternInstance}, while beeing notified from the creation of new instances by the
 * {@link PamelaModelFactory} and {@link PamelaMetaModel}</li>
 * <li>Tagging which methods have to be involved in pattern</li>
 * </ul>
 * 
 * The related {@link AbstractPatternFactory} has the responsability of managing life cycle of instance of {@link PatternDefinition}
 * 
 * @author Caine Silva, Sylvain Guerin
 *
 */
public abstract class PatternDefinition {

	private final String identifier; // identifier as found in annotations
	private final PamelaMetaModel pamelaMetaModel;

	private final Map<Method, List<PatternPrecondition>> preconditions;
	private final Map<Method, List<PatternPostcondition>> postconditions;
	private final Map<Method, List<PatternExceptionHandler>> onExceptions;

	private JavaBindingFactory bindingFactory;

	public PatternDefinition(String identifier, PamelaMetaModel pamelaMetaModel) {
		this.identifier = identifier;
		this.pamelaMetaModel = pamelaMetaModel;
		preconditions = new HashMap<>();
		postconditions = new HashMap<>();
		onExceptions = new HashMap<>();
		bindingFactory = new JavaBindingFactory();
	}

	public String getIdentifier() {
		return identifier;
	}

	public PamelaMetaModel getMetaModel() {
		return pamelaMetaModel;
	}

	public JavaBindingFactory getBindingFactory() {
		return bindingFactory;
	}

	public abstract Class<? extends PatternInstance> getInstanceClass();

	public abstract void finalizeDefinition() throws ModelDefinitionException;

	public boolean isMethodInvolvedInPattern(Method method) {
		for (Method m : preconditions.keySet()) {
			if (PamelaUtils.methodIsEquivalentTo(method, m)) {
				return true;
			}
		}
		for (Method m : postconditions.keySet()) {
			if (PamelaUtils.methodIsEquivalentTo(method, m)) {
				return true;
			}
		}
		return false;
	}

	public List<PatternPrecondition> getPreconditions(Method method) {
		List<PatternPrecondition> returned = preconditions.get(method);
		if (returned == null) {
			for (Method m : preconditions.keySet()) {
				if (PamelaUtils.methodIsEquivalentTo(method, m)) {
					return preconditions.get(m);
				}
			}
		}
		return returned;
	}

	public List<PatternPostcondition> getPostconditions(Method method) {
		List<PatternPostcondition> returned = postconditions.get(method);
		if (returned == null) {
			for (Method m : postconditions.keySet()) {
				if (PamelaUtils.methodIsEquivalentTo(method, m)) {
					return postconditions.get(m);
				}
			}
		}
		return returned;
	}

	public List<PatternExceptionHandler> getOnExceptions(Method method) {
		List<PatternExceptionHandler> returned = onExceptions.get(method);
		if (returned == null) {
			for (Method m : onExceptions.keySet()) {
				if (PamelaUtils.methodIsEquivalentTo(method, m)) {
					return onExceptions.get(m);
				}
			}
		}
		return returned;
	}

	public abstract <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModel model)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public PatternPrecondition addToPreconditionsForMethod(Requires requiresAnnotation, Method method) {
		List<PatternPrecondition> l = preconditions.get(method);
		if (l == null) {
			l = new ArrayList<>();
			preconditions.put(method, l);
		}
		PatternPrecondition newPrecondition = new PatternPrecondition(this, method, requiresAnnotation);
		l.add(newPrecondition);
		return newPrecondition;
	}

	public PatternPostcondition addToPostconditionsForMethod(Ensures ensuresAnnotation, Method method) {
		List<PatternPostcondition> l = postconditions.get(method);
		if (l == null) {
			l = new ArrayList<>();
			postconditions.put(method, l);
		}
		PatternPostcondition newPostcondition = new PatternPostcondition(this, method, ensuresAnnotation);
		l.add(newPostcondition);
		return newPostcondition;
	}

	public PatternExceptionHandler addToOnExceptionForMethod(OnException onExceptionAnnotation, Method method) {
		List<PatternExceptionHandler> l = onExceptions.get(method);
		if (l == null) {
			l = new ArrayList<>();
			onExceptions.put(method, l);
		}
		PatternExceptionHandler newOnException = new PatternExceptionHandler(this, method, onExceptionAnnotation);
		l.add(newOnException);
		return newOnException;
	}

}
