package org.openflexo.pamela.factory;

import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.toolbox.HasPropertyChangeSupport;

public class DefaultSinglePropertyImplementation<I, T> extends AbstractPropertyImplementation<I, T>
		implements SinglePropertyImplementation<I, T> {

	private T internalValue;

	public DefaultSinglePropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property)
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
		if ((aValue == null && get() != null) || (aValue != null && !aValue.equals(get()))) {
			T oldValue = get();
			this.internalValue = aValue;
			if (this instanceof HasPropertyChangeSupport) {
				((HasPropertyChangeSupport) this).getPropertyChangeSupport().firePropertyChange(getProperty().getPropertyIdentifier(),
						oldValue, aValue);
			}
		}
	}

}
