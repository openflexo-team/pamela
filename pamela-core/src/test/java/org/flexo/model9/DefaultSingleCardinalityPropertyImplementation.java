package org.flexo.model9;

import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.toolbox.HasPropertyChangeSupport;

public class DefaultSingleCardinalityPropertyImplementation<I, T> extends AbstractPropertyImplementation<I, T>
		implements SingleCardinalityPropertyImplementation<I, T> {

	private T internalValue;

	public DefaultSingleCardinalityPropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property)
			throws InvalidDataException {
		super(handler, property);
		internalValue = (T) property.getDefaultValue(handler.getModelFactory());
	}

	@Override
	public T get() {
		return internalValue;
	}

	@Override
	public void set(T aValue) {
		if ((aValue == null && internalValue != null) || (aValue != null && !aValue.equals(internalValue))) {
			T oldValue = internalValue;
			this.internalValue = aValue;
			if (this instanceof HasPropertyChangeSupport) {
				((HasPropertyChangeSupport) this).getPropertyChangeSupport().firePropertyChange(getProperty().getPropertyIdentifier(),
						oldValue, aValue);
			}
		}
	}

}
