package org.openflexo.pamela.factory;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.model.ModelProperty;

public class DefaultMultiplePropertyImplementation<I, T> extends AbstractPropertyImplementation<I, List<T>> implements
		MultiplePropertyImplementation<I, T>, SettablePropertyImplementation<I, List<T>>, ReindexableListPropertyImplementation<I, T> {

	private List<T> internalValues;
	private List<T> oldValues;

	public DefaultMultiplePropertyImplementation(ProxyMethodHandler<I> handler, ModelProperty<I> property,
			Class<? extends List> listImplementationClass) throws InvalidDataException, ModelExecutionException {
		super(handler, property);
		try {
			internalValues = listImplementationClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ModelExecutionException(e);
		}
	}

	@Override
	public List<T> get() throws ModelDefinitionException {
		if (getProperty().getGetter() == null) {
			throw new ModelExecutionException("Getter is not defined for property " + getProperty());
		}
		return internalValues;
	}

	@Override
	public void set(List<T> values) throws ModelDefinitionException {
		if (getProperty().getSetter() == null && !getHandler().isDeserializing() && !getHandler().initializing
				&& !getHandler().createdByCloning && !getHandler().deleting) {
			throw new ModelExecutionException("Setter is not defined for property " + getProperty());
		}
		// List<?> oldValues = (List<?>) getHandler().invokeGetter(getProperty());
		List<T> oldValues = internalValues;
		List<T> newValues = new ArrayList();
		if (values != null) {
			newValues.addAll(values);
		}

		for (Object o : new ArrayList<>(oldValues)) {
			if (!newValues.contains(o)) {
				getHandler().invokeRemover(getProperty(), o);
			}
			else {
				newValues.remove(o);
			}
		}
		if (values != null) {
			for (Object o : (List<?>) newValues) {
				getHandler().invokeAdder(getProperty(), o);
			}
		}
	}

	@Override
	public void addTo(T aValue) throws ModelDefinitionException {
		addTo(aValue, -1);
	}

	@Override
	public void addTo(T value, int index) throws ModelDefinitionException {
		if (getProperty().getAdder() == null && !getHandler().isDeserializing() && !getHandler().initializing
				&& !getHandler().createdByCloning && !getHandler().deleting && !getHandler().undeleting) {
			throw new ModelExecutionException("Adder is not defined for property " + getProperty());
		}
		// List<T> list = (List<T>) getHandler().invokeGetter(getProperty());
		List<T> list = internalValues;

		if (getProperty().getAllowsMultipleOccurences() || !list.contains(value)) {
			if (index == -1) {
				list.add(value);
			}
			else {
				list.add(index, value);
			}

			// Notify the change
			firePropertyChange(getProperty().getPropertyIdentifier(), null, value);

			// Handle inverse property for new value
			if (getProperty().hasExplicitInverseProperty() && value != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(value);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException(
							"Opposite entity of " + getProperty() + " is of type " + value.getClass().getName() + " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = getProperty().getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						oppositeHandler.invokeSetter(inverseProperty, getObject());
						break;
					case LIST:
						oppositeHandler.invokeAdder(inverseProperty, getObject());
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
	public void removeFrom(T value) throws ModelDefinitionException {
		if (getProperty().getRemover() == null) {
			throw new ModelExecutionException("Remover is not defined for property " + getProperty());
		}
		// List<T> list = (List<T>) getHandler().invokeGetter(getProperty());
		List<T> list = internalValues;

		if (list.contains(value)) {
			list.remove(value);
			firePropertyChange(getProperty().getPropertyIdentifier(), value, null);
			// Handle inverse property for new value
			if (getProperty().hasExplicitInverseProperty() && value != null) {
				ProxyMethodHandler<Object> oppositeHandler = getModelFactory().getHandler(value);
				if (oppositeHandler == null) {
					// Should not happen
					throw new ModelExecutionException(
							"Opposite entity of " + getProperty() + " is of type " + value.getClass().getName() + " is not a ModelEntity.");
				}
				ModelProperty<? super Object> inverseProperty = getProperty().getInverseProperty(oppositeHandler.getModelEntity());
				switch (inverseProperty.getCardinality()) {
					case SINGLE:
						oppositeHandler.invokeSetter(inverseProperty, null);
						break;
					case LIST:
						oppositeHandler.invokeRemover(inverseProperty, getObject());
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
	public void reindex(T value, int index) throws ModelDefinitionException {

		if (getProperty().getReindexer() == null) {
			throw new ModelExecutionException("Reindexer is not defined for property " + getProperty());
		}
		// List<T> list = (List<T>) getHandler().invokeGetter(getProperty());
		List<T> list = internalValues;

		int oldIndex = list.indexOf(value);

		if (oldIndex > -1) {
			if (oldIndex != index) {
				list.remove(value);
				if (index == -1) {
					list.add(value);
				}
				else {
					list.add(index, value);
				}
				firePropertyChange(getProperty().getPropertyIdentifier(), oldIndex, index);
				if (getProperty().isSerializable()) {
					getHandler().invokeSetModified(true);
				}
			}
			else {
				// Index is already correct
			}
		}
		else {
			System.err.println("Inconsistant data: could not find object: " + value);
		}
	}

	@Override
	public void delete(List<Object> embeddedObjects, Object... context) throws ModelDefinitionException {
		// We retrieve and store old value for a potential undelete
		oldValues = new ArrayList<>((List<T>) getHandler().invokeGetter(getProperty()));

		// Otherwise nullify using setter
		if (getProperty().getSetterMethod() != null) {
			getHandler().invokeSetter(getProperty(), null);
		}
		else {
			getHandler().internallyInvokeSetter(getProperty(), null, true);
		}

		if (oldValues != null) {
			for (Object toBeDeleted : oldValues) {
				if ((toBeDeleted instanceof DeletableProxyObject) && embeddedObjects.contains(toBeDeleted)) {
					// By the way, this object was embedded, delete it
					((DeletableProxyObject) toBeDeleted).delete(context);
					embeddedObjects.remove(toBeDeleted);
				}
			}
		}
	}

	@Override
	public void undelete() throws ModelDefinitionException {
		// Otherwise nullify using setter
		if (getProperty().getSetterMethod() != null) {
			getHandler().invokeSetter(getProperty(), oldValues);
		}
		else {
			getHandler().internallyInvokeSetter(getProperty(), oldValues, true);
		}
	}

}
