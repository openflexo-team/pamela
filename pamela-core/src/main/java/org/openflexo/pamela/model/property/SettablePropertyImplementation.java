package org.openflexo.pamela.model.property;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represent a settable property implementation
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 */
public interface SettablePropertyImplementation<I, T> extends PropertyImplementation<I, T> {

	public void set(T value) throws ModelDefinitionException;

	public void update(T aValue) throws ModelDefinitionException;

}
