package org.openflexo.pamela.model.property;

import java.util.List;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelProperty;

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

	public void delete(List<Object> embeddedObjects, Object... context) throws ModelDefinitionException;

	public void undelete() throws ModelDefinitionException;
}
