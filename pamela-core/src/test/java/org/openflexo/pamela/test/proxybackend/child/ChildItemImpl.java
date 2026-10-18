package org.openflexo.pamela.test.proxybackend.child;

import org.openflexo.pamela.test.proxybackend.model.ItemImpl;

/**
 * Loaded by a child class loader in the tests: an implementation, in a child loader, of an interface of the parent loader
 */
public abstract class ChildItemImpl extends ItemImpl {

	@Override
	public String label() {
		return "child";
	}
}
