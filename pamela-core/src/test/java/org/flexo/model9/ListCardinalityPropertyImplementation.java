package org.flexo.model9;

import java.util.List;

public interface ListCardinalityPropertyImplementation<I, T> extends PropertyImplementation<I, T> {

	@Override
	public List<T> get();

	public void addTo(T aValue);

	public void removeFrom(T aValue);
}
