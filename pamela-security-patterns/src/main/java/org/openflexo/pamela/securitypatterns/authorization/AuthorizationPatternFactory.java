package org.openflexo.pamela.securitypatterns.authorization;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.patterns.AbstractPatternFactory;

public class AuthorizationPatternFactory extends AbstractPatternFactory<AuthorizationPatternDefinition> {

	public AuthorizationPatternFactory(ModelContext modelContext) {
		super(modelContext);
	}

	@Override
	public void discoverEntity(ModelEntity<?> entity) {
	}

}
