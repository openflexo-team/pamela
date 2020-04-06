package org.openflexo.pamela.factory;

import java.util.List;

public interface ListPropertyImplementation<I, T> extends PropertyImplementation<I, List<T>> {

	@Override
	public List<T> get();

	public void addTo(T aValue);

	public void removeFrom(T aValue);
}
