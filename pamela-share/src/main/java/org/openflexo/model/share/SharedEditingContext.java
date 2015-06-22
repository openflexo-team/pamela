package org.openflexo.model.share;

import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelProperty;
import org.openflexo.model.StringConverterLibrary;
import org.openflexo.model.StringConverterLibrary.Converter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.undo.AddCommand;
import org.openflexo.model.undo.AtomicEdit;
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.model.undo.CreateCommand;
import org.openflexo.model.undo.DeleteCommand;
import org.openflexo.model.undo.RemoveCommand;
import org.openflexo.model.undo.SetCommand;
import org.openflexo.model.undo.UndoManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Objects;

/**
 * SharedEditingContext allows to share a model through the network.
 */
public class SharedEditingContext implements EditingContext, PropertyChangeListener {

    /** Context for the model handled by this editing context */
    private final ModelContext modelContext;

    /** Factory to create Change objects meant to be send through the network.*/
    private final ModelFactory changeFactory;

    private final String modelName;

    private final HashMap<String, Object> idToObjects = new HashMap<>();
    private final HashMap<Object, String> objectToIds = new HashMap<>();

    private final UndoManager undoManager = new UndoManager();

    public SharedEditingContext(String modelName, ModelContext context) throws ModelDefinitionException {
        this.modelName = modelName;
        this.modelContext = context;
        undoManager.getPropertyChangeSupport().addPropertyChangeListener(this);
        changeFactory = new ModelFactory(CompoundContent.class);
    }

    public boolean register(Object object) {
        if (object == null) return false;
        String id = computeId(object);
        idToObjects.put(id, object);
        String previousValue = objectToIds.put(object, id);
        return previousValue == null;
    }

    public boolean unregister(Object object) {
        if (object == null) return false;
        String id = objectToIds.remove(object);
        if (id == null) {
            System.err.println("Trying to unregister an object which is not registered");
            return false;
        }

        idToObjects.remove(id);
        return true;
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
        Object value = evt.getNewValue();
        if (value instanceof CompoundEdit) {
            CompoundContent content = transformEdit((CompoundEdit) value);
            System.out.println(content);

        }
    }


    protected CompoundContent transformEdit(CompoundEdit edit) {
        if (edit == null) throw new NullPointerException();

        CompoundContent result = changeFactory.newInstance(CompoundContent.class);
        result.setPresentationName(edit.getPresentationName());
        for (AtomicEdit<?> atomicEdit : edit.getEdits()) {
            result.addToChanges(transformEdit(atomicEdit));
        }
        return result;
    }

    protected Change transformEdit(AtomicEdit<?> edit) {
        if (edit == null) return null;

        if (edit instanceof AddCommand) {
            AddCommand command = (AddCommand) edit;
            return changeFactory.newInstance(
                AddChange.class,
                getId(command.getObject()),
                getModelPropertyName(command.getModelProperty()),
                getId(command.getAddedValue()),
                command.getIndex()
            );

        } else if (edit instanceof RemoveCommand) {
            RemoveCommand command = (RemoveCommand) edit;
            return changeFactory.newInstance(
                RemoveChange.class,
                getId(command.getObject()),
                getModelPropertyName(command.getModelProperty()),
                getId(command.getRemovedValue())
            );

        } else if (edit instanceof CreateCommand) {
            CreateCommand command = (CreateCommand) edit;
            return changeFactory.newInstance(
                CreateChange.class,
                getId(command.getObject())
            );


        } else if (edit instanceof DeleteCommand) {
            DeleteCommand command = (DeleteCommand) edit;
            return changeFactory.newInstance(
                DeleteChange.class,
                getId(command.getObject())
            );


        } else if (edit instanceof SetCommand) {
            SetCommand command = (SetCommand) edit;

            // TODO cache the result for this test to avoid multiple computation
            Object toTest = command.getNewValue() != null ? command.getNewValue() : command.getOldValue();
            // toTest can't be null since at least one for new or old value isn't null
            boolean modelObject = modelContext.getUpperEntities(toTest).size() > 0;

            ObjectDescription oldValueDescription = transformObject(command.getOldValue(), modelObject);
            ObjectDescription newValueDescription = transformObject(command.getNewValue(), modelObject);

            return changeFactory.newInstance(
                SetChange.class,
                getId(command.getObject()),
                getModelPropertyName(command.getModelProperty()),
                oldValueDescription,
                newValueDescription
            );

        } else {
            throw new IllegalArgumentException("Edit type unknown '"+ edit.getClass().getName() + "'");
        }
    }

    protected ObjectDescription transformObject(Object object, boolean model) {
        if (object == null) return null;

        String content;
        if (model) {
            content = getId(object);
        } else {
            @SuppressWarnings("unchecked")
            Converter<Object> converter = (Converter<Object>) StringConverterLibrary.getInstance().getConverter(object.getClass());
            content = converter.convertToString(object);
        }
        return changeFactory.newInstance(ObjectDescription.class, object.getClass().getName(), content);
    }

    protected String getModelPropertyName(ModelProperty<?> property) {
        return property.getModelEntity().getTypeName() + "::" + property.getPropertyIdentifier();
    }

    protected String getId(Object object) {
        if (object == null) return null;
        String id = objectToIds.get(object);
        if (id == null) throw new IllegalArgumentException("Object not register to current shared editing context");
        return id;
    }

    protected CompoundEdit transformCompoundChange(CompoundContent compoundContent) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedEditingContext)) return false;
        SharedEditingContext that = (SharedEditingContext) o;
        return Objects.equals(modelName, that.modelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName);
    }
}
