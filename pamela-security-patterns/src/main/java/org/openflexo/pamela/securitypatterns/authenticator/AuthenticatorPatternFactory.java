package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticateMethod;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticationInformation;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.Authenticator;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorGetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.AuthenticatorSubject;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.ProofOfIdentitySetter;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequestAuthentication;

public class AuthenticatorPatternFactory extends AbstractPatternFactory<AuthenticatorPatternDefinition> {

	public AuthenticatorPatternFactory(ModelContext modelContext) {
		super(modelContext);
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

		for (Method m : entity.getImplementedInterface().getMethods()) {
			RequestAuthentication requestAuthenticationMethodAnnotation = m.getAnnotation(RequestAuthentication.class);
			if (requestAuthenticationMethodAnnotation != null) {
				AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(requestAuthenticationMethodAnnotation.patternID(),
						true);
				patternDefinition.requestAuthentificationMethod = m;
				int i = 0;
				for (AnnotatedType annotatedType : m.getAnnotatedParameterTypes()) {
					System.out.println("i=" + i + " annotatedType = " + annotatedType);
				}

			}
			AuthenticationInformation authInfoAnnotation = m.getAnnotation(AuthenticationInformation.class);
			if (authInfoAnnotation != null) {
				AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(authInfoAnnotation.patternID(), true);
				patternDefinition.authentificationInfoMethod = m;
			}
			ProofOfIdentitySetter proofOfIdentitySetterAnnotation = m.getAnnotation(ProofOfIdentitySetter.class);
			if (proofOfIdentitySetterAnnotation != null) {
				AuthenticatorPatternDefinition patternDefinition = getPatternDefinition(proofOfIdentitySetterAnnotation.patternID(), true);
				patternDefinition.proofOfIdentitySetterMethod = m;
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

}
