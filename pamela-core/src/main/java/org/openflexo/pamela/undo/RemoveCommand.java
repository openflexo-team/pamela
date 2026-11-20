/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
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

package org.openflexo.pamela.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.pamela.model.ModelProperty;
import org.openflexo.pamela.model.property.MultiplePropertyImplementation;
import org.openflexo.pamela.model.property.PropertyImplementation;
import org.openflexo.pamela.model.property.ReindexableListPropertyImplementation;

/**
 * This command corresponds to the REMOVING of an object.<br>
 * This command addresses one property and one value (the value beeing removed)
 * 
 * @author sylvain
 * 
 * @param <I>
 *            type of object on which this removing occurs
 */
public class RemoveCommand<I> extends AtomicEdit<I> {

	private I updatedObject;
	private Object removedValue;
	private ModelProperty<? super I> modelProperty;
	/** Index of the removed value in its list when it was removed (-1 if unknown or not a list): the undo puts it back there */
	private int index = -1;

	public RemoveCommand(I updatedObject, ModelEntity<I> modelEntity, ModelProperty<? super I> modelProperty, Object removedValue,
			PamelaModelFactory pamelaModelFactory) {
		super(modelEntity, pamelaModelFactory);
		this.updatedObject = updatedObject;
		this.modelProperty = modelProperty;
		this.removedValue = removedValue;
		this.index = indexOfValue(pamelaModelFactory, updatedObject, modelProperty, removedValue);
	}

	private static <I> int indexOfValue(PamelaModelFactory factory, I object, ModelProperty<? super I> property, Object value) {
		if (property == null || property.getCardinality() != Cardinality.LIST) {
			return -1;
		}
		ProxyMethodHandler<I> handler = factory.getHandler(object);
		if (handler == null) {
			return -1;
		}
		// Read the list stored by PAMELA itself, not through the getter: a getter may be overridden, derived or resolve lazily
		PropertyImplementation<? super I, ?> impl = handler.getPropertyImplementation(property);
		if (impl instanceof MultiplePropertyImplementation) {
			try {
				Object values = ((MultiplePropertyImplementation<?, ?>) impl).get();
				return values instanceof List ? ((List<?>) values).indexOf(value) : -1;
			} catch (ModelDefinitionException e) {
				return -1;
			}
		}
		return -1;
	}

	@Override
	public I getObject() {
		return updatedObject;
	}

	public ModelProperty<? super I> getModelProperty() {
		return modelProperty;
	}

	public Object getRemovedValue() {
		return removedValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		ProxyMethodHandler<I> handler = getModelFactory().getHandler(updatedObject);
		handler.invokeAdder(modelProperty, removedValue);
		if (index > -1) {
			// The adder appends: put the value back where it was (PAMELA-D-8)
			PropertyImplementation<? super I, ?> impl = handler.getPropertyImplementation(modelProperty);
			if (impl instanceof ReindexableListPropertyImplementation && impl instanceof MultiplePropertyImplementation) {
				try {
					List<?> values = (List<?>) ((MultiplePropertyImplementation<?, ?>) impl).get();
					if (values != null && values.contains(removedValue)) {
						((ReindexableListPropertyImplementation<I, Object>) impl).reindex(removedValue,
								Math.min(index, values.size() - 1));
					}
				} catch (ModelDefinitionException e) {
					throw new ModelExecutionException(e);
				}
			}
		}
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {
		getModelFactory().getHandler(updatedObject).invokeRemover(modelProperty, removedValue);
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public void die() {
		modelProperty = null;
		updatedObject = null;
		removedValue = null;
		super.die();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return "REMOVE " + updatedObject + " property=" + (modelProperty != null ? modelProperty.getPropertyIdentifier() : null)
				+ " removedValue=" + removedValue;
	}

	@Override
	public String getType() {
		return "REMOVE";
	}

}
