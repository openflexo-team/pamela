package org.openflexo.pamela.model.property;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represents a {@link PropertyImplementation} which is settable and of SINGLE cardinality
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 */
public interface SinglePropertyImplementation<I, T> extends SettablePropertyImplementation<I, T> {

	@Override
	public T get() throws ModelDefinitionException;

	@Override
	public void set(T aValue) throws ModelDefinitionException;
}
