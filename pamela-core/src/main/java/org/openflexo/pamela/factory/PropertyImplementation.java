package org.openflexo.pamela.factory;

import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represent a particular property implementation
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 */
public interface PropertyImplementation<I, T> {

	public ModelProperty<I> getProperty();

	public I getObject();

	public T get() throws ModelDefinitionException;

}
