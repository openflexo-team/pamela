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

import javax.swing.undo.UndoableEdit;

import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.model.ModelEntity;

/**
 * This is an atomic edit managed by PAMELA.<br>
 * This edit is defined in the context of a given {@link ModelFactory} which should be passed at construction<br>
 * An {@link AtomicEdit} always addresses a {@link ModelEntity}
 * 
 * @author sylvain
 * 
 * @param <I>
 */
public abstract class AtomicEdit<I> implements UndoableEdit {

	private ModelFactory modelFactory;
	private ModelEntity<I> modelEntity;

	public AtomicEdit(ModelEntity<I> modelEntity, ModelFactory modelFactory) {
		this.modelEntity = modelEntity;
		this.modelFactory = modelFactory;
	}

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

	public ModelEntity<I> getModelEntity() {
		return modelEntity;
	}

	public abstract I getObject();

	@Override
	public final boolean addEdit(UndoableEdit anEdit) {
		// Atomic edits are not aggregable
		return false;
	}

	@Override
	public final boolean replaceEdit(UndoableEdit anEdit) {
		// Atomic edits are not aggregable
		return false;
	}

	@Override
	public void die() {
		modelEntity = null;
		modelFactory = null;
	}

	@Override
	public String getUndoPresentationName() {
		return "UNDO " + getPresentationName();
	}

	@Override
	public String getRedoPresentationName() {
		return "REDO" + getPresentationName();
	}

	@Override
	public String toString() {
		return getPresentationName();
	}

	public abstract String getType();
}
