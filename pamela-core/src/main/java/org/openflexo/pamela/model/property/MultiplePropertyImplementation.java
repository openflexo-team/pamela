package org.openflexo.pamela.model.property;

import java.util.List;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

public interface MultiplePropertyImplementation<I, T> extends PropertyImplementation<I, List<T>> {

	@Override
	public List<T> get() throws ModelDefinitionException;

	public void addTo(T aValue) throws ModelDefinitionException;

	public void addTo(T aValue, int index) throws ModelDefinitionException;

	public void removeFrom(T aValue) throws ModelDefinitionException;
}
