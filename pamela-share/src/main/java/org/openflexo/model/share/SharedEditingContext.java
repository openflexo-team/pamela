package org.openflexo.model.share;

import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.undo.UndoManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * Created by j5r on 14/06/15.
 */
public class SharedEditingContext implements EditingContext, PropertyChangeListener {

    private final HashMap<String, Object> idToObjects = new HashMap<String, Object>();
    private final HashMap<Object, String> objectToIds = new HashMap<Object, String>();

    private final UndoManager undoManager = new UndoManager();

    public SharedEditingContext() {
        undoManager.getPropertyChangeSupport().addPropertyChangeListener(this);
    }

    public boolean register(Object object) {
        return false;
    }

    public boolean unregister(Object object) {
        return false;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    protected String computeId(Object object) {
        if (object == null) return null;
        // TODO implements a valid id computation
        return object.toString();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(evt);
    }
}
