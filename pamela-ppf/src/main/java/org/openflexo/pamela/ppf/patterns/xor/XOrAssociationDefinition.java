/**
 * 
 * Copyright (c) 2013-2020, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of pamela-security-patterns, a component of the software infrastructure 
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
package org.openflexo.pamela.ppf.patterns.xor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.patterns.PatternDefinition;

/**
 * Represents an occurence of a <code>XOrAssociation</code>
 * 
 * @author Sylvain Guerin
 *
 */
public class XOrAssociationDefinition<I> extends PatternDefinition {

	public static final String SOURCE_ROLE = "Source";

	public ModelEntity<I> sourceEntity;
	public ModelProperty<? super I> property1;
	public ModelProperty<? super I> property2;

	public XOrAssociationDefinition(String identifier, PamelaMetaModel pamelaMetaModel) {
		super(identifier, pamelaMetaModel);
	}

	@Override
	public Class<? extends XOrAssociationInstance> getInstanceClass() {
		return XOrAssociationInstance.class;
	}

	@Override
	public void finalizeDefinition() throws ModelDefinitionException {

		if (sourceEntity == null) {
			throw new ModelDefinitionException("Inconsistent XOrAssociation " + getIdentifier() + ": no source entity ");
		}
		if (property1 == null || property2 == null) {
			throw new ModelDefinitionException("Inconsistent XOrAssociation " + getIdentifier() + ": missing property ");
		}
	}

	@Override
	public <I2> void notifiedNewInstance(I2 newInstance, ModelEntity<I2> modelEntity, PamelaModel model)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (modelEntity == sourceEntity) {
			// We create a new PatternInstance for each new instance of subjectModelEntity
			XOrAssociationInstance<I> newPatternInstance = new XOrAssociationInstance(this, model, newInstance);
		}
	}

	@Override
	public boolean isMethodInvolvedInPattern(Method method) {
		return super.isMethodInvolvedInPattern(method);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("XOrAssociationDefinition\n");
		sb.append("sourceEntity=" + sourceEntity + "\n");
		sb.append("property1=" + property1 + "\n");
		sb.append("property2=" + property2 + "\n");
		return sb.toString();
	}

	public boolean isValid() {
		// Perform here required checks
		return true;
	}

}
