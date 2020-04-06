package org.openflexo.pamela.factory;

public interface ReindexableListPropertyImplementation<I, T> extends ListPropertyImplementation<I, T> {

	public void reindex(T aValue, int index);
}
