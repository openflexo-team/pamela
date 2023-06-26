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

import java.lang.reflect.Method;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;

/**
 * Represent the factory for {@link XOrAssociationDefinition}
 * 
 * @author Sylvain Guerin
 */
public class XOrAssociationFactory extends AbstractPatternFactory<XOrAssociationDefinition> {

	public XOrAssociationFactory(PamelaMetaModel pamelaMetaModel) {
		super(pamelaMetaModel);
	}

	@Override
	protected void discoverMethod(Method m) throws ModelDefinitionException {

		super.discoverMethod(m);

		XOrAssociation xOrAnnotation = m.getAnnotation(XOrAssociation.class);
		if (xOrAnnotation != null) {
			XOrAssociationDefinition patternDefinition = getPatternDefinition(xOrAnnotation.patternID(), true);
			ModelEntity<?> sourceEntity = getPamelaMetaModel().getEntityForGetter(m);
			if (patternDefinition.sourceEntity == null) {
				patternDefinition.sourceEntity = sourceEntity;
			}
			else if (patternDefinition.sourceEntity != sourceEntity) {
				throw new ModelDefinitionException("Inconsistant source entities for XOrAssociation " + xOrAnnotation.patternID() + " : "
						+ patternDefinition.sourceEntity + " and " + sourceEntity);
			}

			if (patternDefinition.property1 == null) {
				patternDefinition.property1 = getPamelaMetaModel().getEntityForGetter(m).getPropertyForMethod(m);
			}
			else if (patternDefinition.property2 == null) {
				patternDefinition.property2 = getPamelaMetaModel().getEntityForGetter(m).getPropertyForMethod(m);
			}
			else {
				throw new ModelDefinitionException(
						"Inconsistant XOrAssociation " + xOrAnnotation.patternID() + " : more than two relations are defined");
			}
		}
	}

}
