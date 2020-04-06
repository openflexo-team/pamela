package org.openflexo.pamela.factory;

public interface SinglePropertyImplementation<I, T> extends SettablePropertyImplementation<I, T> {

	@Override
	public T get();

	@Override
	public void set(T aValue);
}
