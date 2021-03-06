package org.openflexo.pamela.test.propertyimplementation;

import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.property.DefaultSinglePropertyImplementation;

public class MySingleCardinalityPropertyImplementation<I, T> extends DefaultSinglePropertyImplementation<I, T> {

	public MySingleCardinalityPropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property) throws InvalidDataException {
		super(handler, property);
	}

	@Override
	public void set(T aValue) throws ModelDefinitionException {
		super.set(aValue);
		System.out.println("Coucou je passe la pour " + getProperty() + " value=" + aValue);
	}
}
