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

package org.openflexo.pamela.factory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.patterns.ExecutionMonitor;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.patterns.PatternInstance;
import org.openflexo.pamela.undo.CreateCommand;

/**
 * A {@link PamelaModel} represents an instance of a {@link PamelaMetaModel}
 * 
 * It contains all the instances of entities defined in related {@link PamelaMetaModel}
 * 
 * @author sylvain
 *
 */
public class PamelaModel {

	private static final Logger logger = Logger.getLogger(PamelaModel.class.getPackage().getName());

	private final PamelaMetaModel metaModel;

	private EditingContext editingContext;

	private final Set<ExecutionMonitor> executionMonitors;
	private Map<Object, Set<PatternInstance<?>>> patternInstances = new HashMap<>();
	private Map<PatternDefinition, Set<PatternInstance<?>>> registeredPatternInstances = new HashMap<>();

	public PamelaModel(PamelaMetaModel metaModel) {
		this.metaModel = metaModel;
		executionMonitors = new HashSet<>();
	}

	public PamelaMetaModel getMetaModel() {
		return metaModel;
	}

	/**
	 * Return {@link EditingContext} associated with this factory.
	 * 
	 * @return
	 */
	public EditingContext getEditingContext() {
		return editingContext;
	}

	/**
	 * Sets {@link EditingContext} associated with this factory.<br>
	 * When not null, new instances created with this factory are automatically registered in this EditingContext
	 * 
	 * @param editingContext
	 */
	public void setEditingContext(EditingContext editingContext) {
		this.editingContext = editingContext;
	}

	// Patterns

	public void addExecutionMonitor(ExecutionMonitor m) {
		this.executionMonitors.add(m);
	}

	public Set<ExecutionMonitor> getExecutionMonitors() {
		return this.executionMonitors;
	}

	public boolean removeExecutionMonitor(ExecutionMonitor m) {
		return this.executionMonitors.remove(m);
	}

	public <P extends PatternDefinition> void registerPatternInstance(PatternInstance<P> patternInstance) {
		P definition = patternInstance.getPatternDefinition();
		Set<PatternInstance<?>> s = registeredPatternInstances.get(definition);
		if (s == null) {
			s = new HashSet<>();
			registeredPatternInstances.put(definition, s);
		}
		System.out.println("Registering " + patternInstance);
		s.add(patternInstance);
	}

	public void registerStakeHolderForPatternInstance(Object stakeHolder, String role, PatternInstance<?> patternInstance) {
		Set<PatternInstance<?>> s = patternInstances.get(stakeHolder);
		if (s == null) {
			s = new HashSet<>();
			patternInstances.put(stakeHolder, s);
		}
		System.out.println("Registering " + stakeHolder + " as " + role + " for pattern instance " + patternInstance);
		s.add(patternInstance);
	}

	public Set<PatternInstance<?>> getPatternInstances(Object stakeholder) {
		return patternInstances.get(stakeholder);
	}

	public <P extends PatternDefinition> Set<PatternInstance<P>> getPatternInstances(P patternDefinition) {
		return (Set) registeredPatternInstances.get(patternDefinition);
	}

	/*
	 * Called by the PamelaModelFactory
	 */
	protected <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModelFactory modelFactory) {
		// Iterate on all pattern factories declared in the metamodel, and notify creation of new instance
		for (AbstractPatternFactory<?> patternFactory : getMetaModel().getPatternFactories()) {
			for (PatternDefinition patternDefinition : patternFactory.getPatternDefinitions().values()) {
				patternDefinition.notifiedNewInstance(newInstance, modelEntity, this);
			}
		}
		if (getEditingContext() != null) {
			if (getEditingContext().getUndoManager() != null) {
				getEditingContext().getUndoManager().addEdit(new CreateCommand<>(newInstance, modelEntity, modelFactory));
			}
		}

	}

}
