package org.openflexo.pamela.patterns;

import java.lang.reflect.Method;

import org.openflexo.pamela.ModelContext;
import org.openflexo.pamela.ModelEntity;

public abstract class PatternDefinition {

	private final String identifier; // identifier as found in annotations
	private final ModelContext modelContext;

	public PatternDefinition(String identifier, ModelContext modelContext) {
		this.identifier = identifier;
		this.modelContext = modelContext;
	}

	public String getIdentifier() {
		return identifier;
	}

	public ModelContext getModelContext() {
		return modelContext;
	}

	public abstract boolean isMethodInvolvedInPattern(Method m);

	public abstract <I> void notifiedNewInstance(I newInstance, ModelEntity<I> modelEntity);

}
