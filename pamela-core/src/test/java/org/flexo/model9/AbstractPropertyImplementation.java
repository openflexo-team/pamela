package org.flexo.model9;

import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.factory.ProxyMethodHandler;

public abstract class AbstractPropertyImplementation<I, T> implements PropertyImplementation<I, T> {

	private final ProxyMethodHandler<I> handler;
	private final ModelProperty<I> property;

	public AbstractPropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property) {
		this.handler = handler;
		this.property = property;
	}

	protected ProxyMethodHandler<I> getHandler() {
		return handler;
	}

	public I getObject() {
		return getHandler().getObject();
	}

	@Override
	public ModelProperty<I> getProperty() {
		return property;
	}

}
