package org.openflexo.pamela.test.proxybackend.model.impl;

import java.io.IOException;

import org.openflexo.pamela.test.proxybackend.model.Item;

/**
 * Implementation in another package than the interface
 */
public abstract class OtherItemImpl implements Item {

	@Override
	public String label() {
		return "other";
	}

	@Override
	public String describe() {
		return "Other " + getName();
	}

	@Override
	public void fail() throws IOException {
		throw new IOException("expected");
	}
}
