package org.openflexo.pamela.model.property;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represents a {@link PropertyImplementation} which is settable and of single cardinality
 * 
 * @author sylvain
 *
 * @param <I>
 * @param <T>
 */
public interface SinglePropertyImplementation<I, T> extends SettablePropertyImplementation<I, T> {

	@Override
	public T get() throws ModelDefinitionException;

	@Override
	public void set(T aValue) throws ModelDefinitionException;
}
