package org.openflexo.pamela.factory;

import java.beans.PropertyChangeSupport;
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
		return IProxyMethodHandler.isEqual(oldValue, newValue, seen);

	}

}
