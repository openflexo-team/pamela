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
		objects = new HashSet<Object>();
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
