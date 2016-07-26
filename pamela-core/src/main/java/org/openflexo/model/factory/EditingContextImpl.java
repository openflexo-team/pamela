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

package org.openflexo.model.factory;

import java.util.HashSet;
import java.util.Set;

import org.openflexo.model.undo.UndoManager;

/**
 * Default implementation for {@link EditingContext} interface.
 * 
 * @author sylvain
 * 
 */
public class EditingContextImpl implements EditingContext {

	private final Set<Object> objects;
	private UndoManager undoManager;

	public EditingContextImpl() {
		objects = new HashSet<>();
	}

	/**
	 * Register supplied object in this EditingContext
	 * 
	 * @param object
	 * @return boolean indicating if registering was successfull
	 */
	@Override
	public boolean register(Object object) {
		if (!objects.contains(object)) {
			objects.add(object);
			return true;
		}
		return false;
	}

	/**
	 * Unregister supplied object from this EditingContext
	 * 
	 * @param object
	 * @return boolean indicating if un-registering was successfull
	 */
	@Override
	public boolean unregister(Object object) {
		if (objects.contains(object)) {
			objects.remove(object);
			return true;
		}
		System.err.println("Trying to unregister an object which is not registered");
		return false;
	}

	/**
	 * Creates and register an UndoManager tracking edits on this ModelFactory
	 * 
	 * @return
	 */
	public UndoManager createUndoManager() {
		undoManager = new UndoManager();
		return undoManager;
	}

	/**
	 * Return UndoManager associated with this {@link EditingContext} when any.
	 * 
	 * @return
	 */
	@Override
	public UndoManager getUndoManager() {
		return undoManager;
	}

}
