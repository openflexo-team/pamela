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
package org.openflexo.pamela.securitypatterns.customauthenticator.model;

import java.lang.reflect.InvocationTargetException;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.factory.PamelaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternDefinition;

/**
 * A specialization for {@link AuthenticatorPatternDefinition}
 */
public class CustomAuthenticatorPatternDefinition extends AuthenticatorPatternDefinition {

	public CustomAuthenticatorPatternDefinition(String identifier, PamelaMetaModel pamelaMetaModel) {
		super(identifier, pamelaMetaModel);
	}

	@Override
	public Class<CustomAuthenticatorPatternInstance> getInstanceClass() {
		return CustomAuthenticatorPatternInstance.class;
	}

	@Override
	public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity, PamelaModel model) {
		// System.out.println("notifiedNewInstance " + newInstance);
		if (modelEntity == subjectModelEntity) {
			// We create a new PatternInstance for each new instance of subjectModelEntity
			try {
				CustomAuthenticatorPatternInstance newPatternInstance = new CustomAuthenticatorPatternInstance(this, model,
						(Subject) newInstance);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}