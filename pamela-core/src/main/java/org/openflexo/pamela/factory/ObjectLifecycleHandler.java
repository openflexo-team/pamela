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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.annotations.ComplexEmbedded;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.property.PropertyImplementation;
import org.openflexo.pamela.undo.CreateCommand;
import org.openflexo.pamela.undo.DeleteCommand;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Handles the lifecycle (delete, undelete, destroy) of a PAMELA object.<br>
 * Manages cascading deletion based on {@link Embedded} and {@link ComplexEmbedded} annotations.<br>
 * Extracted from {@link ProxyMethodHandler} to isolate lifecycle concerns.
 *
 * @author sylvain
 * @param <I> type of object managed by the parent {@link ProxyMethodHandler}
 */
public class ObjectLifecycleHandler<I> {

	private final ProxyMethodHandler<I> handler;

	boolean deleted = false;
	boolean deleting = false;
	boolean undeleting = false;
	private boolean destroyed = false;

	public ObjectLifecycleHandler(ProxyMethodHandler<I> handler) {
		this.handler = handler;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isDeleting() {
		return deleting;
	}

	public boolean isUndeleting() {
		return undeleting;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * Deletes the current object and all its embedded properties as defined by the {@link Embedded} and {@link ComplexEmbedded}
	 * annotations. The provided <code>context</code> represents a list of objects that will also be eventually deleted and which should be
	 * taken into account when computing embedded objects according to deletion conditions.
	 *
	 * @param trackAtomicEdit whether to register this deletion in the UndoManager
	 * @param context         the list of objects that will also be deleted
	 * @see Embedded#deletionConditions()
	 * @see ComplexEmbedded#deletionConditions()
	 */
	public boolean delete(boolean trackAtomicEdit, Object... context) throws ModelDefinitionException {

		if (deleted || deleting) {
			return false;
		}

		deleting = true;
		if (context == null) {
			context = new Object[] { handler.getObject() };
		}
		else {
			context = Arrays.copyOf(context, context.length + 1);
			context[context.length - 1] = handler.getObject();
		}

		ModelEntity<I> modelEntity = handler.getModelEntity();
		Set<Object> objects = new HashSet<>();
		for (Object o : context) {
			objects.add(o);
		}
		List<Object> embeddedObjects = handler.getModelFactory().getEmbeddedObjects(handler.getObject(),
				EmbeddingType.DELETION, objects.toArray(new Object[0]));
		objects.addAll(embeddedObjects);
		context = objects.toArray(new Object[0]);

		Iterator<ModelProperty<? super I>> i = modelEntity.getProperties();
		while (i.hasNext()) {
			ModelProperty<? super I> property = i.next();
			if (!property.getType().isPrimitive()) {
				PropertyImplementation<? super I, ?> propertyImplementation = handler.getPropertyImplementation(property);
				propertyImplementation.delete(embeddedObjects, context);
			}
		}

		// Warn if embedded objects were not deleted through normal property cascade
		for (Object object : embeddedObjects) {
			if (object instanceof DeletableProxyObject) {
				DeletableProxyObject objectToDelete = (DeletableProxyObject) object;
				if (!objectToDelete.isDeleted()) {
					objectToDelete.delete(context);
					System.err.println("This is weird: this object was embedded but not deleted: " + objectToDelete);
				}
			}
		}

		if (trackAtomicEdit && handler.getUndoManager() != null) {
			handler.getUndoManager().addEdit(new DeleteCommand<>(handler.getObject(), handler.getModelEntity(), handler.getModelFactory()));
		}

		deleted = true;
		deleting = false;

		// Notify via HasPropertyChangeSupport if available
		if (handler.getObject() instanceof HasPropertyChangeSupport) {
			HasPropertyChangeSupport object = (HasPropertyChangeSupport) handler.getObject();
			object.getPropertyChangeSupport().firePropertyChange(object.getDeletedProperty(), false, true);
		}

		// Also notify via core PropertyChangeSupport
		// TODO: maybe we have to check that it is not the same PropertyChangeSupport?
		handler.getNotificationHandler().fireDeleted();

		return deleted;
	}

	/**
	 * Undeletes the current object, optionally restoring all its properties to their pre-deletion values.
	 *
	 * @param restoreProperties whether to restore property values
	 * @param trackAtomicEdit   whether to register this undeletion in the UndoManager
	 */
	public boolean undelete(boolean restoreProperties, boolean trackAtomicEdit) throws ModelDefinitionException {

		if (!deleted || deleting) {
			return false;
		}

		undeleting = true;

		if (trackAtomicEdit && handler.getUndoManager() != null) {
			handler.getUndoManager()
					.addEdit(new CreateCommand<>(handler.getObject(), handler.getModelEntity(), handler.getModelFactory()));
		}

		if (restoreProperties) {
			ModelEntity<I> modelEntity = handler.getModelEntity();
			Iterator<ModelProperty<? super I>> i = modelEntity.getProperties();
			while (i.hasNext()) {
				ModelProperty<? super I> property = i.next();
				if (!property.getType().isPrimitive()) {
					PropertyImplementation<? super I, ?> propertyImplementation = handler.getPropertyImplementation(property);
					propertyImplementation.undelete();
				}
			}
		}

		deleted = false;
		undeleting = false;
		handler.getNotificationHandler().fireUndeleted();
		return deleted;
	}

	/**
	 * Destroys the current object.<br>
	 * After invoking this, the object won't be accessible and all operations on it will be in undetermined state.<br>
	 * To implement delete/undelete facilities, use {@link DeletableProxyObject} instead.
	 */
	public void destroy() {
		handler.clearPropertyImplementations();
		destroyed = true;
	}
}
