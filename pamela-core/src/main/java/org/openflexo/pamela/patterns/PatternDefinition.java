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

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * Abstract base class for an occurence of an <code>Pattern</code>. An instance is uniquely identified by the <code>patternID</code> field
 * of associated annotations.<br>
 * 
 * It has the responsibility of:
 * <ul>
 * <li>Managing life-cycle of related {@link PatternInstance}, while beeing notified from the creation of new instances by the
 * {@link ModelFactory} and {@link ModelContext}</li>
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
	private final ModelContext modelContext;

	public PatternDefinition(String identifier, ModelContext modelContext) {
		this.identifier = identifier;
		this.modelContext = modelContext;
	}

	public String getIdentifier() {
		return identifier;
	}

	public ModelContext getModelContext() {
		return modelContext;
	}

	public abstract void finalizeDefinition() throws ModelDefinitionException;

	public abstract boolean isMethodInvolvedInPattern(Method m);

	public abstract <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity);

}
