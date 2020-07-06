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

import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * This command corresponds to the CREATION of an object.<br>
 * This command addresses one model entity (type of newly instantiated object)
 * 
 * @author sylvain
 * 
 * @param <I>
 *            type of object beeing created
 */
public class CreateCommand<I> extends AtomicEdit<I> {

	private I createdObject;

	public CreateCommand(I createdObject, ModelEntity<I> modelEntity, ModelFactory modelFactory) {
		super(modelEntity, modelFactory);
		this.createdObject = createdObject;
	}

	@Override
	public I getObject() {
		return createdObject;
	}

	@Override
	public void undo() throws CannotUndoException {
		getModelFactory().getHandler(createdObject).invokeDeleter(createdObject);
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {
		// Tricky area
		// We have here to restore the properties
		getModelFactory().getHandler(createdObject).invokeUndeleter(true);
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public void die() {
		createdObject = null;
		super.die();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return "CREATE " + createdObject;
	}

	@Override
	public String getType() {
		return "CREATE";
	}

}
