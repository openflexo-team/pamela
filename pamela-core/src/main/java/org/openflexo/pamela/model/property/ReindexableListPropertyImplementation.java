package org.openflexo.pamela.model.property;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Represent a reindexable property implementation (which has MULTIPLE cardinality)
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 */
public interface ReindexableListPropertyImplementation<I, T> extends MultiplePropertyImplementation<I, T> {

	public void reindex(T aValue, int index) throws ModelDefinitionException;
}
