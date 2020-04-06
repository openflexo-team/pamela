package org.flexo.model9;

import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.factory.DefaultSinglePropertyImplementation;
import org.openflexo.pamela.factory.ProxyMethodHandler;

public class MySingleCardinalityPropertyImplementation<I, T> extends DefaultSinglePropertyImplementation<I, T> {

	public MySingleCardinalityPropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property) throws InvalidDataException {
		super(handler, property);
	}

	@Override
	public void set(T aValue) {
		super.set(aValue);
		System.out.println("Coucou je passe la pour " + getProperty() + " value=" + aValue);
	}
}
