package org.openflexo.pamela.test.proxybackend.model;

import java.io.IOException;

/**
 * Implementation in the package of the interface
 */
public abstract class ItemImpl implements Item {

	// Computed in the constructor, before any handler is installed
	public final String labelAtConstruction;

	public ItemImpl() {
		labelAtConstruction = label();
	}

	// Re-declared abstract here, without the @Initializer of the interface
	@Override
	public abstract Item initRedeclared(String name);

	@Override
	public String label() {
		return "label";
	}

	@Override
	public String describe() {
		return "Item " + getName();
	}

	@Override
	public void fail() throws IOException {
		throw new IOException("expected");
	}
}
