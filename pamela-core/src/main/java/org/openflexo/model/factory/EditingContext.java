package org.openflexo.model.factory;

import org.openflexo.model.undo.UndoManager;

/**
 * Represents an {@link EditingContext} managing a collection of PAMELA objects<br>
 * 
 * Objects might be registered and unregistered to/from EditingContext.<br>
 * An {@link UndoManager} might be declared for an {@link EditingContext}.
 * 
 * @author sylvain
 * 
 */
public interface EditingContext {

	/**
	 * Register supplied object in this EditingContext
	 * 
	 * @param object
	 * @return boolean indicating if registering was successfull
	 */
	public boolean register(Object object);

	/**
	 * Unregister supplied object from this EditingContext
	 * 
	 * @param object
	 * @return boolean indicating if un-registering was successfull
	 */
	public boolean unregister(Object object);

	/**
	 * Return UndoManager associated with this {@link EditingContext} when any.
	 * 
	 * @return
	 */
	public UndoManager getUndoManager();
}
