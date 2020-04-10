package org.openflexo.pamela.securitypatterns.authorization;

import org.openflexo.pamela.patterns.PatternDefinition;

public class AuthorizationPatternDefinition extends PatternDefinition {

	public AuthorizationPatternDefinition(String identifier) {
		super(identifier);
	}

	@Override
	public void notifiedNewInstance(Object newInstance) {
		System.out.println("Tiens on cree un " + newInstance + " of " + newInstance.getClass());
	}

	public boolean isValid() {
		// Perform here required checks
		return true;
	}

}
