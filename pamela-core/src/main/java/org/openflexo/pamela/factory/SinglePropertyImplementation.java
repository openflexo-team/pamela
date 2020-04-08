package org.openflexo.pamela.factory;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

public interface SinglePropertyImplementation<I, T> extends SettablePropertyImplementation<I, T> {

	@Override
	public T get() throws ModelDefinitionException;

	@Override
	public void set(T aValue) throws ModelDefinitionException;
}
