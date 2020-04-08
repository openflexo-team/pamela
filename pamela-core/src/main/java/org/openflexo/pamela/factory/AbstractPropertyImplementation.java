package org.openflexo.pamela.factory;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Set;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.factory.ModelFactory.PAMELAProxyFactory;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Base abstract class providing property implementation
 * 
 * 
 * @author sylvain
 *
 * @param <I>
 *            type of entity defining such property
 * @param <T>
 *            accessed type for the property
 * @param <M>
 *            internal memory adressable for a given entity instance and property
 */
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

	@Override
	public I getObject() {
		return getHandler().getObject();
	}

	public ModelFactory getModelFactory() {
		return getHandler().getModelFactory();
	}

	final public ModelEntity<I> getModelEntity() {
		return getHandler().getModelEntity();
	}

	public PAMELAProxyFactory<I> getPamelaProxyFactory() {
		return getHandler().getPamelaProxyFactory();
	}

	@Override
	public ModelProperty<I> getProperty() {
		return property;
	}

	protected void firePropertyChange(String propertyIdentifier, Object oldValue, Object value) {
		if (getObject() instanceof HasPropertyChangeSupport && !getHandler().deleting) {
			PropertyChangeSupport propertyChangeSupport = ((HasPropertyChangeSupport) getObject()).getPropertyChangeSupport();
			if (propertyChangeSupport != null) {
				propertyChangeSupport.firePropertyChange(propertyIdentifier, oldValue, value);
			}
		}
	}

	protected static boolean isEqual(Object oldValue, Object newValue, Set<Object> seen) {
		seen.add(oldValue);
		if (oldValue == null) {
			return newValue == null;
		}
		if (oldValue == newValue) {
			return true;
		}
		if (oldValue instanceof AccessibleProxyObject && newValue instanceof AccessibleProxyObject) {
			return ((AccessibleProxyObject) oldValue).equalsObject(newValue);
		}
		if (oldValue instanceof List && newValue instanceof List) {
			List<Object> l1 = (List<Object>) oldValue;
			List<Object> l2 = (List<Object>) newValue;
			if (l1.size() != l2.size()) {
				return false;
			}
			for (int i = 0; i < l1.size(); i++) {
				Object v1 = l1.get(i);
				Object v2 = l2.get(i);
				if (seen.contains(v1))
					continue;

				if (!isEqual(v1, v2, seen)) {
					return false;
				}
			}
			return true;
		}
		return oldValue.equals(newValue);

	}

}
