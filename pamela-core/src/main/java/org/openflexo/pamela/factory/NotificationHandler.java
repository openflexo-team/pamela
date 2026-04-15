/**
 *
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 *
 * This file is part of Pamela-core, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or
 *          combining it with software containing parts covered by the terms
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.pamela.factory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.toolbox.HasPropertyChangeSupport;

import javassist.util.proxy.ProxyObject;

/**
 * Handles property change notifications and serialization/modification state for a PAMELA object.<br>
 * Manages the {@link PropertyChangeSupport}, the modified/serializing/deserializing flags,
 * and propagation of the modified state to forwarded objects.<br>
 * Extracted from {@link ProxyMethodHandler} to isolate notification concerns.
 *
 * @author sylvain
 * @param <I> type of object managed by the parent {@link ProxyMethodHandler}
 */
public class NotificationHandler<I> implements PropertyChangeListener {

	private final ProxyMethodHandler<I> handler;

	private PropertyChangeSupport propertyChangeSupport;
	private boolean serializing = false;
	private boolean deserializing = false;
	private boolean modified = false;

	public NotificationHandler(ProxyMethodHandler<I> handler) {
		this.handler = handler;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		if (propertyChangeSupport == null) {
			propertyChangeSupport = new PropertyChangeSupport(handler.getObject());
		}
		return propertyChangeSupport;
	}

	// =========================================================================
	// Serializing / deserializing
	// =========================================================================

	public boolean isSerializing() {
		return serializing;
	}

	public void setSerializing(boolean serializing, boolean resetModifiedStatus) throws ModelDefinitionException {
		if (this.serializing != serializing) {
			this.serializing = serializing;
			firePropertyChange(IProxyMethodHandler.SERIALIZING, !serializing, serializing);
			if (resetModifiedStatus && !serializing) {
				setModified(false);
			}
		}
	}

	public boolean isDeserializing() {
		return deserializing;
	}

	public void setDeserializing(boolean deserializing) {
		if (this.deserializing != deserializing) {
			this.deserializing = deserializing;
			if (deserializing) {
				handler.initialized = true;
			}
			else {
				modified = false;
			}
			firePropertyChange(IProxyMethodHandler.DESERIALIZING, !deserializing, deserializing);
		}
	}

	// =========================================================================
	// Modified
	// =========================================================================

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) throws ModelDefinitionException {
		if (modified) {
			if (!isDeserializing() && !isSerializing()) {
				boolean old = this.modified;
				this.modified = modified;
				if (!old) {
					firePropertyChange(IProxyMethodHandler.MODIFIED, old, modified);
					if (handler.getModelEntity().getModify() != null && handler.getModelEntity().getModify().forward() != null) {
						ModelProperty<? super I> modelProperty = handler.getModelEntity()
								.getModelProperty(handler.getModelEntity().getModify().forward());
						if (modelProperty != null) {
							Object forward = handler.invokeGetter(modelProperty);
							if (forward instanceof ProxyObject) {
								((ProxyMethodHandler<?>) ((ProxyObject) forward).getHandler()).getNotificationHandler()
										.setModifiedPublic(modified);
							}
						}
					}
				}
			}
		}
		else if (this.modified != modified) {
			this.modified = modified;
			firePropertyChange(IProxyMethodHandler.MODIFIED, !modified, modified);
		}
	}

	/** Called from forwarded objects via PropertyChangeListener */
	void setModifiedPublic(boolean modified) throws ModelDefinitionException {
		setModified(modified);
	}

	// =========================================================================
	// PropertyChange
	// =========================================================================

	@Deprecated
	public void firePropertyChange(String propertyIdentifier, Object oldValue, Object value) {
		if (handler.getObject() instanceof HasPropertyChangeSupport && !handler.getLifecycleHandler().isDeleting()) {
			PropertyChangeSupport pcs = ((HasPropertyChangeSupport) handler.getObject()).getPropertyChangeSupport();
			if (pcs != null) {
				pcs.firePropertyChange(propertyIdentifier, oldValue, value);
			}
		}
	}

	/** Fires the DELETED event and removes all listeners */
	void fireDeleted() {
		getPropertyChangeSupport().firePropertyChange(IProxyMethodHandler.DELETED, false, true);
		// TODO ASK Syl if we should not remove all the listeners from pcSupport here?
		// TODO => notify the listener when it forgot to stop listening
		for (PropertyChangeListener cl : propertyChangeSupport.getPropertyChangeListeners()) {
			propertyChangeSupport.removePropertyChangeListener(cl);
		}
		propertyChangeSupport = null;
	}

	void fireUndeleted() {
		getPropertyChangeSupport().firePropertyChange(IProxyMethodHandler.UNDELETED, false, true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			if (handler.getModelEntity().getModify() != null && handler.getModelEntity().getModify().synchWithForward()) {
				Object forwarded = handler.internallyInvokeGetter(handler.getModelEntity().getModify().forward());
				if (evt.getSource() == forwarded) {
					if (IProxyMethodHandler.MODIFIED.equals(evt.getPropertyName())) {
						setModified((Boolean) evt.getNewValue());
					}
				}
			}
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}
}
