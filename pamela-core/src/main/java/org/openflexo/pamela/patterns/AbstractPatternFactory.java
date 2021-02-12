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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.annotations.Ensures;
import org.openflexo.pamela.patterns.annotations.Requires;

/**
 * Abstract base class for a {@link PatternDefinition} factory
 * 
 * 
 * @author sylvain
 *
 * @param <P>
 */
public abstract class AbstractPatternFactory<P extends PatternDefinition> {

	private Map<String, P> patternDefinitions;
	private ModelContext modelContext;

	public AbstractPatternFactory(ModelContext modelContext) {
		patternDefinitions = new HashMap<>();
		this.modelContext = modelContext;
	}

	protected Class<? extends P> getPatternDefinitionClass() {
		return (Class<P>) TypeUtils.getTypeArgument(getClass(), AbstractPatternFactory.class, 0);
	}

	protected P getPatternDefinition(String patternId, boolean createWhenNonExistant) {
		P returned = patternDefinitions.get(patternId);
		if (returned == null && createWhenNonExistant) {
			try {
				Constructor<? extends P> constructor = getPatternDefinitionClass().getConstructor(String.class, ModelContext.class);
				returned = constructor.newInstance(patternId, modelContext);
				patternDefinitions.put(patternId, returned);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return returned;
	}

	public Map<String, P> getPatternDefinitions() {
		return patternDefinitions;
	}

	public void discoverEntity(ModelEntity<?> entity) {
		for (Method m : entity.getImplementedInterface().getMethods()) {
			discoverMethod(m);
		}
	}

	protected void discoverMethod(Method m) {
		Requires requiresAnnotation = m.getAnnotation(Requires.class);
		if (requiresAnnotation != null) {
			PatternDefinition patternDefinition = getPatternDefinition(requiresAnnotation.patternID(), false);
			if (patternDefinition != null) {
				patternDefinition.addToPreconditionsForMethod(requiresAnnotation, m);
			}
		}
		Ensures ensuresAnnotation = m.getAnnotation(Ensures.class);
		if (ensuresAnnotation != null) {
			PatternDefinition patternDefinition = getPatternDefinition(ensuresAnnotation.patternID(), false);
			if (patternDefinition != null) {
				patternDefinition.addToPostconditionsForMethod(ensuresAnnotation, m);
			}
		}
	}
}
