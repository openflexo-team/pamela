package org.openflexo.pamela.model.property;

import org.openflexo.pamela.exceptions.ModelDefinitionException;

public interface ReindexableListPropertyImplementation<I, T> extends MultiplePropertyImplementation<I, T> {

	public void reindex(T aValue, int index) throws ModelDefinitionException;
}
