package org.openflexo.pamela.securitypatterns.authenticator;

import java.lang.reflect.Method;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.patterns.PatternDefinition;

public class AuthenticatorPatternDefinition extends PatternDefinition {

	public ModelEntity<?> authenticatorModelEntity; // @Authenticator
	public Method requestAuthentificationMethod; // @RequestAuthentication
	public int authentificationInfoParameterIndex; // @AuthenticationInformation in parameter

	public ModelEntity<?> subjectModelEntity; // @AuthenticatorSubject
	public Method authentificationInfoMethod; // @AuthenticationInformation
	public Method proofOfIdentitySetterMethod; // @ProofOfIdentitySetter
	public Method authenticatorGetterMethod; // @AuthenticatorGetter
	public Method authenticateMethod; // @AuthenticateMethod

	public AuthenticatorPatternDefinition(String identifier) {
		super(identifier);
	}

	@Override
	public void notifiedNewInstance(Object newInstance) {
		System.out.println("Tiens on cree un " + newInstance + " of " + newInstance.getClass());
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
