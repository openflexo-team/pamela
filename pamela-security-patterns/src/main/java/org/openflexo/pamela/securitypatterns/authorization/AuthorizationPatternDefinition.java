package org.openflexo.pamela.securitypatterns.authorization;

import java.lang.reflect.Method;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.patterns.PatternDefinition;

public class AuthorizationPatternDefinition extends PatternDefinition {

	public AuthorizationPatternDefinition(String identifier, ModelContext modelContext) {
		super(identifier, modelContext);
	}

	@Override
	public void finalizeDefinition() throws ModelDefinitionException {
		// TODO Auto-generated method stub

	}

	@Override
	public <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity) {
		System.out.println("Tiens on cree un " + newInstance + " of " + newInstance.getClass());
	}

	public boolean isValid() {
		// Perform here required checks
		return true;
	}

	@Override
	public boolean isMethodInvolvedInPattern(Method m) {
		// TODO Auto-generated method stub
		return false;
	}
}
