package org.flexo.model9;

import org.openflexo.pamela.ModelProperty;

public interface PropertyImplementation<I, T> {

	public ModelProperty<I> getProperty();

	public Object get();

}
