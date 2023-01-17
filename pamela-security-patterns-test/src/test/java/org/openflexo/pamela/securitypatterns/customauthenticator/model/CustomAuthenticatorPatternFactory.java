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

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.securitypatterns.authenticator.AuthenticatorPatternFactory;

/**
 * A specialization for {@link AuthenticatorPatternFactory}
 */
public class CustomAuthenticatorPatternFactory extends AuthenticatorPatternFactory {

	public CustomAuthenticatorPatternFactory(PamelaMetaModel metaModel) {
		super(metaModel);
	}

	@Override
	protected Class<CustomAuthenticatorPatternDefinition> getPatternDefinitionClass() {
		return CustomAuthenticatorPatternDefinition.class;
	}

	@Override
	protected CustomAuthenticatorPatternDefinition getPatternDefinition(String patternId, boolean createWhenNonExistant) {
		return (CustomAuthenticatorPatternDefinition) super.getPatternDefinition(patternId, createWhenNonExistant);
	}

}
