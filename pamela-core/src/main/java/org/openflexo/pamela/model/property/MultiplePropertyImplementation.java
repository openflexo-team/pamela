package org.openflexo.pamela.model.property;

import java.util.List;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represents a {@link PropertyImplementation} which is settable and of MULTIPLE cardinality
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 */
public interface MultiplePropertyImplementation<I, T> extends PropertyImplementation<I, List<T>> {

	@Override
	public List<T> get() throws ModelDefinitionException;

	public void addTo(T aValue) throws ModelDefinitionException;

	public void addTo(T aValue, int index) throws ModelDefinitionException;

	public void removeFrom(T aValue) throws ModelDefinitionException;
}
