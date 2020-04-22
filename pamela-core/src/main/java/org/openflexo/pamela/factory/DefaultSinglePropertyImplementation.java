package org.openflexo.pamela.factory;

import java.util.List;
import java.util.StringTokenizer;

import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import javassist.util.proxy.ProxyObject;

public class DefaultSinglePropertyImplementation<I, T> extends AbstractPropertyImplementation<I, T>
		implements SinglePropertyImplementation<I, T> {

	private T internalValue = null;
	private T oldValue = null;

	public DefaultSinglePropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property) throws InvalidDataException {
		super(handler, property);
	}

	@Override
	public T get() throws ModelDefinitionException {
		if (getProperty().getGetter() == null) {
			throw new ModelExecutionException("Getter is not defined for property " + getProperty());
		}
		if (getProperty().getReturnedValue() != null) {
			// Simple implementation of ReturnedValue. This can be drastically improved
			String returnedValue = getProperty().getReturnedValue().value();
			StringTokenizer st = new StringTokenizer(returnedValue, ".");
			Object value = this;
			ProxyMethodHandler<?> handler = getHandler();
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				value = handler.invokeGetter(token);
				if (value != null) {
					if (st.hasMoreTokens()) {
						if (!(value instanceof ProxyObject)) {
							throw new ModelExecutionException("Cannot invoke " + st.nextToken() + " on object of type "
									+ value.getClass().getName() + " (caused by returned value: " + returnedValue + ")");
						}
						handler = (ProxyMethodHandler<?>) ((ProxyObject) value).getHandler();
					}
				}
				else {
					return null;
				}
			}
			return (T) value;
		}
		T returned = internalValue;
		if (returned != null) {
			return returned;
		}
		else {
			T defaultValue;
			try {
				defaultValue = (T) getProperty().getDefaultValue(getModelFactory());
			} catch (InvalidDataException e) {
				throw new ModelExecutionException("Invalid default value '" + getProperty().getGetter().defaultValue() + "' for property "
						+ getProperty() + " with type " + getProperty().getType(), e);
			}
			if (defaultValue != null) {
				internalValue = defaultValue;
				return defaultValue;
			}
			if (getProperty().getType().isPrimitive()) {
				throw new ModelExecutionException("No default value defined for primitive property " + getProperty());
			}
			return null;
		}
		// return internalValue;
	}

	@Override
	public void set(T aValue) throws ModelDefinitionException {
		/*if ((aValue == null && get() != null) || (aValue != null && !aValue.equals(get()))) {
			T oldValue = get();
			this.internalValue = aValue;
			if (this instanceof HasPropertyChangeSupport) {
				((HasPropertyChangeSupport) this).getPropertyChangeSupport().firePropertyChange(getProperty().getPropertyIdentifier(),
						oldValue, aValue);
			}
		}*/

		if (getHandler().getScheduledSets().get(getProperty()) == aValue) {
			// This set was already scheduled (we are entering in an infinite loop): break NOW
			return;
		}

		getHandler().getScheduledSets().remove(getProperty());

		// System.out.println("Object " + getModelEntity().getImplementedInterface().getSimpleName() + " set "
		// + property.getPropertyIdentifier() + " with " + value);

		if (getProperty().getSetter() == null && !getHandler().isDeserializing() && !getHandler().initializing
				&& !getHandler().createdByCloning && !getHandler().deleting && !getHandler().undeleting) {
			throw new ModelExecutionException("Setter is not defined for property " + getProperty());
		}
		// Object oldValue = invokeGetter(property);
		T oldValue = (T) getHandler().invokeGetter(getProperty());

		// Is it a real change ?
		if (!isEqual(oldValue, aValue)) {
			// System.out.println("Change for " + oldValue + " to " + value);
			boolean hasInverse = getProperty().hasExplicitInverseProperty();

			getHandler().getScheduledSets().put(getProperty(), aValue);

			// First handle inverse property for oldValue
			if (hasInverse && oldValue != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(oldValue);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException("Opposite entity of " + getProperty() + " is of type " + oldValue.getClass().getName()
							+ " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = getProperty().getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						Object oppositeOldValue = oppositeHandler.invokeGetter(inverseProperty);
						if (oppositeOldValue != null) {
							// System.out.println("Object " + inverseProperty.getModelEntity().getImplementedInterface().getSimpleName() +
							// " set "
							// + inverseProperty.getPropertyIdentifier() + " with " + null);
							oppositeHandler.invokeSetter(inverseProperty, null);
						}
						else {
							// No need to reset inverse setter, as it is already set to null
						}
						break;
					case LIST:
						// TODO: what is same object has multiple occurences in the list ???
						List<Object> oppositeListValue = (List<Object>) oppositeHandler.invokeGetter(inverseProperty);
						if (oppositeListValue.contains(getObject())) {
							oppositeHandler.invokeRemover(inverseProperty, getObject());
						}
						else {
							// No need to remove objet from opposite property object was not inside
						}
						break;
					default:
						throw new ModelExecutionException("Invalid cardinality: " + inverseProperty.getCardinality());
				}
			}

			// Now do the job, internally
			/*if (value == null) {
				values.remove(getProperty().getPropertyIdentifier());
			}
			else {
				values.put(getProperty().getPropertyIdentifier(), value);
			}*/

			internalValue = aValue;

			// Notify the change
			firePropertyChange(getProperty().getPropertyIdentifier(), oldValue, aValue);

			if (getModelEntity().getModify() != null && getModelEntity().getModify().synchWithForward()
					&& getProperty().getPropertyIdentifier().equals(getModelEntity().getModify().forward())) {
				if (oldValue instanceof HasPropertyChangeSupport) {
					((HasPropertyChangeSupport) oldValue).getPropertyChangeSupport()
							.removePropertyChangeListener(ProxyMethodHandler.MODIFIED, getHandler());
				}
				if (aValue instanceof HasPropertyChangeSupport) {
					((HasPropertyChangeSupport) aValue).getPropertyChangeSupport().addPropertyChangeListener(ProxyMethodHandler.MODIFIED,
							getHandler());
				}
			}
			// Now handle inverse property for newValue
			if (hasInverse && aValue != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(aValue);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException("Opposite entity of " + getProperty() + " is of type " + aValue.getClass().getName()
							+ " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = getProperty().getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						Object oppositeOldValue = oppositeHandler.invokeGetter(inverseProperty);
						if (oppositeOldValue != getObject()) {
							// System.out.println("Object " + inverseProperty.getModelEntity().getImplementedInterface().getSimpleName() +
							// " set "
							// + inverseProperty.getPropertyIdentifier() + " with " + getObject());
							oppositeHandler.invokeSetter(inverseProperty, getObject());
						}
						else {
							// No need to set inverse property, because this is already right value
						}
						break;
					case LIST:
						// TODO: what is same object has multiple occurences in the list ???
						List<Object> oppositeListValue = (List<Object>) oppositeHandler.invokeGetter(inverseProperty);
						if (!oppositeListValue.contains(getObject())) {
							oppositeHandler.invokeAdder(inverseProperty, getObject());
						}
						else {
							// No need to add object to inverse property, because this is already inside
						}
						break;
					default:
						throw new ModelExecutionException("Invalid cardinality: " + inverseProperty.getCardinality());
				}
			}

			if (getProperty().isSerializable()) {
				getHandler().invokeSetModified(true);
			}

		}

	}

	@Override
	public void delete(List<Object> embeddedObjects, Object... context) throws ModelDefinitionException {

		// We retrieve and store old value for a potential undelete
		oldValue = (T) getHandler().invokeGetter(getProperty());

		// Otherwise nullify using setter
		if (getProperty().getSetterMethod() != null) {
			getHandler().invokeSetter(getProperty(), null);
		}
		else {
			getHandler().internallyInvokeSetter(getProperty(), null, true);
		}

		if ((oldValue instanceof DeletableProxyObject) && embeddedObjects.contains(oldValue)) {
			// By the way, this object was embedded, delete it
			((DeletableProxyObject) oldValue).delete(context);
			embeddedObjects.remove(oldValue);
		}

	}

	@Override
	public void undelete() throws ModelDefinitionException {
		// Otherwise nullify using setter
		if (getProperty().getSetterMethod() != null) {
			getHandler().invokeSetter(getProperty(), oldValue);
		}
		else {
			getHandler().internallyInvokeSetter(getProperty(), oldValue, true);
		}
	}

}
