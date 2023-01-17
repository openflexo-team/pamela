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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentityGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

/**
 * Represent the factory for {@link AuthenticatorPatternDefinition}
 * 
 * @author Caine Silva, Sylvain Guerin
 */
public class AuthenticatorPatternFactory extends AbstractPatternFactory<AuthenticatorPatternDefinition> {

	public AuthenticatorPatternFactory(PamelaMetaModel pamelaMetaModel) {
		super(pamelaMetaModel);
	}

	@Override
	public void discoverEntity(ModelEntity<?> entity) {
		for (Annotation a : entity.getImplementedInterface().getAnnotations()) {
			if (a instanceof AuthenticatorSubject) {
				AuthenticatorSubject subjectAnnotation = (AuthenticatorSubject) a;
				String patternId = subjectAnnotation.patternID();
				AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(patternId, true);
				patternDefinition.subjectModelEntity = entity;
			}
			if (a instanceof Authenticator) {
				Authenticator authenticatorAnnotation = (Authenticator) a;
				String patternId = authenticatorAnnotation.patternID();
				AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(patternId, true);
				patternDefinition.authenticatorModelEntity = entity;
			}
		}

		super.discoverEntity(entity);

		for (AuthenticatorPatternDefinition patternDefinition : getPatternDefinitions().values()) {
			Collections.sort(patternDefinition.authentificationInfoMethods, new Comparator<Method>() {
				@Override
				public int compare(Method m1, Method m2) {
					return authenticationInformationIndexes.get(m1) - authenticationInformationIndexes.get(m2);
				}
			});
		}
	}

	private Map<Method, Integer> authenticationInformationIndexes = new HashMap<>();

	@Override
	protected void discoverMethod(Method m) {

		super.discoverMethod(m);

		RequestAuthentication requestAuthenticationMethodAnnotation = m.getAnnotation(RequestAuthentication.class);
		if (requestAuthenticationMethodAnnotation != null) {
			AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(requestAuthenticationMethodAnnotation.patternID(),
					true);
			patternDefinition.requestAuthentificationMethod = m;
			// TODO: investigate on this
			/*int i = 0;
			for (AnnotatedType annotatedType : m.getAnnotatedParameterTypes()) {
				System.out.println("i=" + i + " annotatedType = " + annotatedType + " pour " + m);
				i++;
				AuthenticationInformation annotation = annotatedType.getAnnotation(AuthenticationInformation.class);
				System.out.println("annotation=" + annotation);
				System.out.println("annotations=" + annotatedType.getAnnotations().length);
			}*/

		}
		AuthenticationInformation authInfoAnnotation = m.getAnnotation(AuthenticationInformation.class);
		if (authInfoAnnotation != null) {
			AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(authInfoAnnotation.patternID(), true);
			patternDefinition.authentificationInfoMethods.add(m);
			if (authInfoAnnotation.isUniqueKey()) {
				patternDefinition.authentificationInfoUniqueKeyMethods.add(m);
			}
			authenticationInformationIndexes.put(m, authInfoAnnotation.index());
		}
		ProofOfIdentitySetter proofOfIdentitySetterAnnotation = m.getAnnotation(ProofOfIdentitySetter.class);
		if (proofOfIdentitySetterAnnotation != null) {
			AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(proofOfIdentitySetterAnnotation.patternID(), true);
			patternDefinition.proofOfIdentitySetterMethod = m;
		}
		ProofOfIdentityGetter proofOfIdentityGetterAnnotation = m.getAnnotation(ProofOfIdentityGetter.class);
		if (proofOfIdentityGetterAnnotation != null) {
			AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(proofOfIdentityGetterAnnotation.patternID(), true);
			patternDefinition.proofOfIdentityGetterMethod = m;
		}
		AuthenticatorGetter authenticatorGetterAnnotation = m.getAnnotation(AuthenticatorGetter.class);
		if (authenticatorGetterAnnotation != null) {
			AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(authenticatorGetterAnnotation.patternID(), true);
			patternDefinition.authenticatorGetterMethod = m;
		}
		AuthenticateMethod authenticateMethodAnnotation = m.getAnnotation(AuthenticateMethod.class);
		if (authenticateMethodAnnotation != null) {
			AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(authenticateMethodAnnotation.patternID(), true);
			patternDefinition.authenticateMethod = m;
		}
	}

}
