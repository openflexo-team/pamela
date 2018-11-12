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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openflexo.pamela.ModelEntity;
import org.openflexo.pamela.ModelProperty;
import org.openflexo.pamela.factory.CloneableProxyObject;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * This command corresponds to the SETTING of a value of an object.<br>
 * This command addresses one property, the old value and the new value
 * 
 * @author sylvain
 * 
 * @param <I>
 *            type of object on which this adding occurs
 */
public class SetCommand<I> extends AtomicEdit<I> {

	private I updatedObject;
	private Object oldValue;
	private Object newValue;
	private ModelProperty<? super I> modelProperty;

	public SetCommand(I updatedObject, ModelEntity<I> modelEntity, ModelProperty<? super I> modelProperty, Object oldValue,
			Object newValue, ModelFactory modelFactory) {
		super(modelEntity, modelFactory);
		this.updatedObject = updatedObject;
		this.modelProperty = modelProperty;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public I getObject() {
		return updatedObject;
	}

	public ModelProperty<? super I> getModelProperty() {
		return modelProperty;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		getModelFactory().getHandler(updatedObject).invokeSetter(modelProperty, oldValue);
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {
		getModelFactory().getHandler(updatedObject).invokeSetter(modelProperty, newValue);
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public void die() {
		super.die();
		modelProperty = null;
		updatedObject = null;
		oldValue = null;
		newValue = null;
	}

	/**
	 * Returns true if this edit is considered significant.<br>
	 * A significant SET command is a set changing the current value for a new value. If both values are equals, edit is not signifiant. If
	 * both values equals, but declare to be {@link CloneableProxyObject}, edit is considered to be signifiant. Otherwise, the edit is
	 * signifiant
	 * 
	 * @return true if this edit is significant
	 */
	@Override
	public boolean isSignificant() {
		if (oldValue == newValue) {
			return false;
		}
		if (oldValue == null) {
			return newValue != null;
		}
		if (oldValue instanceof CloneableProxyObject) {
			return true;
		}
		return !oldValue.equals(newValue);
	}

	@Override
	public String getPresentationName() {
		return "SET " + updatedObject + " property=" + (modelProperty != null ? modelProperty.getPropertyIdentifier() : null)
				+ " oldValue=" + oldValue + " newValue=" + newValue;
	}

	@Override
	public String getType() {
		return "SET";
	}

}
