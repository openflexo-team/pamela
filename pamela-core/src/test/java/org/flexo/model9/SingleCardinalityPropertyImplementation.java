package org.flexo.model9;

public interface SingleCardinalityPropertyImplementation<I, T> extends PropertyImplementation<I, T> {

	@Override
	public T get();

	public void set(T aValue);
}
