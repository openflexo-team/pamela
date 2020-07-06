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
package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.factory.PamelaUtils;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;

/**
 * Represents an occurence of an <code>Authenticator Pattern</code>. An instance is uniquely identified by the <code>patternID</code> field
 * of associated annotations.<br>
 * 
 * It has the responsibility of:
 * <ul>
 * <li>Managing life-cycle of {@link AuthenticatorPatternInstance}, while beeing notified from the creation of new instances by the
 * {@link ModelFactory} and {@link ModelContext}</li>
 * <li>Tagging which methods have to be involved in pattern</li>
 * </ul>
 * 
 * @author Caine Silva, Sylvain Guerin
 *
 */
public class AuthenticatorPatternDefinition extends PatternDefinition {

	public static final String SUBJECT_ROLE = "Subject";
	public static final String AUTHENTICATOR_ROLE = "Authenticator";

	public ModelEntity<?> authenticatorModelEntity; // @Authenticator
	public Method requestAuthentificationMethod; // @RequestAuthentication
	public int authentificationInfoParameterIndex; // @AuthenticationInformation in parameter

	public ModelEntity<?> subjectModelEntity; // @AuthenticatorSubject
	public Method authentificationInfoMethod; // @AuthenticationInformation
	public Method proofOfIdentitySetterMethod; // @ProofOfIdentitySetter
	public Method authenticatorGetterMethod; // @AuthenticatorGetter
	public Method authenticateMethod; // @AuthenticateMethod

	public Method proofOfIdentityGetterMethod; // derived property

	public AuthenticatorPatternDefinition(String identifier, ModelContext modelContext) {
		super(identifier, modelContext);
	}

	@Override
	public void finalizeDefinition() throws ModelDefinitionException {

		if (proofOfIdentityGetterMethod == null) {
			// attempt to retrieve it
			Iterator properties = subjectModelEntity.getProperties();
			while (properties.hasNext()) {
				ModelProperty<?> p = (ModelProperty<?>) properties.next();
				if (PamelaUtils.methodIsEquivalentTo(proofOfIdentitySetterMethod, p.getSetterMethod())) {
					proofOfIdentityGetterMethod = p.getGetterMethod();
				}
			}
		}
		if (proofOfIdentityGetterMethod == null) {
			throw new ModelDefinitionException("No getter for Proof of identity in " + subjectModelEntity.getImplementedInterface());
		}

	}

	@Override
	public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
		// System.out.println("notifiedNewInstance " + newInstance);
		if (modelEntity == subjectModelEntity) {
			// We create a new PatternInstance for each new instance of subjectModelEntity
			AuthenticatorPatternInstance<?, I, ?, ?> newPatternInstance = new AuthenticatorPatternInstance(this, newInstance);
		}
	}

	@Override
	public boolean isMethodInvolvedInPattern(Method method) {
		if (PamelaUtils.methodIsEquivalentTo(method, requestAuthentificationMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, authentificationInfoMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, proofOfIdentitySetterMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, authenticatorGetterMethod)
				|| PamelaUtils.methodIsEquivalentTo(method, authenticateMethod)) {
			return true;
		}
		if (method.getAnnotation(RequiresAuthentication.class) != null) {
			return true;
		}
		try {
			Method apiMethod = subjectModelEntity.getImplementedInterface().getMethod(method.getName(), method.getParameterTypes());
			if (apiMethod.getAnnotation(RequiresAuthentication.class) != null) {
				return true;
			}
		} catch (NoSuchMethodException e) {
			// Not found
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("AuthenticatorPatternDefinition\n");
		sb.append("authenticatorModelEntity=" + authenticatorModelEntity + "\n");
		sb.append("requestAuthentificationMethod=" + requestAuthentificationMethod + "\n");
		sb.append("authentificationInfoParameterIndex=" + authentificationInfoParameterIndex + "\n");
		sb.append("subjectModelEntity=" + subjectModelEntity + "\n");
		sb.append("authentificationInfoMethod=" + authentificationInfoMethod + "\n");
		sb.append("proofOfIdentitySetterMethod=" + proofOfIdentitySetterMethod + "\n");
		sb.append("authenticatorGetterMethod=" + authenticatorGetterMethod + "\n");
		sb.append("authenticateMethod=" + authenticateMethod + "\n");
		return sb.toString();
	}

	public boolean isValid() {
		// Perform here required checks
		return true;
	}

}
