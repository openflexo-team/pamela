package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.reflect.Method;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.PamelaUtils;
import org.openflexo.pamela.patterns.PatternDefinition;
import org.openflexo.pamela.securitypatterns.authenticator.annotations.RequiresAuthentication;

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

	public AuthenticatorPatternDefinition(String identifier, ModelContext modelContext) {
		super(identifier, modelContext);
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
