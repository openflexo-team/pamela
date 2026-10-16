package org.openflexo.pamela.test.implementingclass.impl;

import org.openflexo.pamela.test.implementingclass.Item;

/**
 * An implementation of {@link Item}, deliberately in another package than the interface
 */
public abstract class ItemImpl implements Item {

	@Override
	public String describe() {
		return "Item " + getName();
	}
}
