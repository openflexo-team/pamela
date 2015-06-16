package org.openflexo.model.share;

import org.junit.Test;
import org.openflexo.model.ModelContext;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.model.undo.UndoManager;

/**
 * Class of test to check if the share basics mechanism works
 */
public class Tests  {

    @Test
    public void test1() throws Exception {
        ModelContext modelContext = new ModelContext(Message.class);
        ModelFactory factory = new ModelFactory(modelContext);

        final EditingContext editingContext = new SharedEditingContext();
        factory.setEditingContext(editingContext);
        UndoManager undoManager = editingContext.getUndoManager();
        System.out.println(undoManager);

        CompoundEdit test1 = undoManager.startRecording("test1");

        Message m1 = factory.newInstance(Message.class);
        m1.setText("Message 1");

        Message m2 = factory.newInstance(Message.class);
        m2.setText("Message 2");
        m1.addReply(m2);

        undoManager.stopRecording(test1);


    }
}
