package org.openflexo.pamela.patterns;

public abstract class PatternDefinition {

	private final String identifier; // identifier as found in annotations

	public PatternDefinition(String identifier) {
		this.identifier = identifier;
	}

	public abstract void notifiedNewInstance(Object newInstance);

}
