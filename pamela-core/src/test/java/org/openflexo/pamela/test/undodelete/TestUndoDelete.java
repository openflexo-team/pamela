package org.openflexo.pamela.test.undodelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.factory.EditingContextImpl;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.pamela.undo.UndoManager;

/**
 * PAMELA-D-8: undoing the deletion of an object restores it, its embedded objects, and the order of the lists.
 */
public class TestUndoDelete {

	private PamelaModelFactory factory;
	private UndoManager undoManager;

	@Before
	public void setUp() throws Exception {
		factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(TreeNode.class));
		EditingContextImpl editingContext = new EditingContextImpl();
		editingContext.createUndoManager();
		factory.setEditingContext(editingContext);
		undoManager = editingContext.getUndoManager();
	}

	private TreeNode node(String name) {
		TreeNode n = factory.newInstance(TreeNode.class);
		n.setName(name);
		return n;
	}

	@Test
	public void undoingTheDeletionOfAContainerRestoresIt() throws Exception {
		TreeNode top = node("top");
		TreeNode other = node("other");
		TreeNode root1 = node("root1");
		TreeNode alpha = node("alpha");
		TreeNode leaf = node("leaf");
		TreeNode beta = node("beta");
		top.addToChildren(other);
		top.addToChildren(root1);
		root1.addToChildren(alpha);
		root1.addToChildren(beta);
		alpha.addToChildren(leaf);

		if (undoManager.isBeeingRecording()) {
			// The construction above leaves an unidentified recording open
			undoManager.stopRecording(undoManager.getCurrentEdition());
		}
		CompoundEdit delete = undoManager.startRecording("delete");
		root1.delete();
		undoManager.stopRecording(delete);
		assertEquals(List.of(other), top.getChildren());

		undoManager.undo();

		for (TreeNode n : List.of(root1, alpha, leaf, beta)) {
			assertFalse(n.getName() + " still deleted", n.isDeleted());
		}
		assertEquals(List.of(other, root1), top.getChildren());
		assertEquals(List.of(alpha, beta), root1.getChildren());
		assertEquals(List.of(leaf), alpha.getChildren());
		assertSame(top, root1.getParent());
		assertSame(root1, alpha.getParent());

		undoManager.redo();
		assertEquals(List.of(other), top.getChildren());
		assertEquals(true, root1.isDeleted());
		undoManager.undo();
		assertEquals(List.of(other, root1), top.getChildren());
		assertEquals(List.of(alpha, beta), root1.getChildren());
	}
}
