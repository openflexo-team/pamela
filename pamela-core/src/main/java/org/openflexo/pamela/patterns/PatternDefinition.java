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

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.PamelaUtils;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.annotations.Ensures;
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

	private final Map<Method, List<Requires>> preconditions;
	private final Map<Method, List<Ensures>> postconditions;

	public PatternDefinition(String identifier, PamelaMetaModel pamelaMetaModel) {
		this.identifier = identifier;
		this.pamelaMetaModel = pamelaMetaModel;
		preconditions = new HashMap<>();
		postconditions = new HashMap<>();
	}

	public String getIdentifier() {
		return identifier;
	}

	public PamelaMetaModel getMetaModel() {
		return pamelaMetaModel;
	}

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

	public List<Requires> getPreconditions(Method method) {
		List<Requires> returned = preconditions.get(method);
		if (returned == null) {
			for (Method m : preconditions.keySet()) {
				if (PamelaUtils.methodIsEquivalentTo(method, m)) {
					return preconditions.get(m);
				}
			}
		}
		return returned;
	}

	public List<Ensures> getPostconditions(Method method) {
		List<Ensures> returned = postconditions.get(method);
		if (returned == null) {
			for (Method m : postconditions.keySet()) {
				if (PamelaUtils.methodIsEquivalentTo(method, m)) {
					return postconditions.get(m);
				}
			}
		}
		return returned;
	}

	public abstract <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModel model)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public void addToPreconditionsForMethod(Requires precondition, Method method) {
		List<Requires> l = preconditions.get(method);
		if (l == null) {
			l = new ArrayList<>();
			preconditions.put(method, l);
		}
		l.add(precondition);
	}

	public void addToPostconditionsForMethod(Ensures postcondition, Method method) {
		List<Ensures> l = postconditions.get(method);
		if (l == null) {
			l = new ArrayList<>();
			postconditions.put(method, l);
		}
		l.add(postcondition);
	}

}
