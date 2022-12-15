package org.openflexo.pamela.test.tests1;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.factory.EditingContextImpl;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.test.AbstractPAMELATest;
import org.openflexo.pamela.undo.CompoundEdit;
import org.openflexo.pamela.undo.UndoManager;

/**
 * Class of test to check if the undo/redo behavior is correct.
 */
public class UndoRedoTests extends AbstractPAMELATest {

	private PamelaModelFactory factory;
	private PamelaMetaModel pamelaMetaModel;
	private UndoManager undoManager;

	@Override
	@Before
	public void setUp() throws Exception {
		pamelaMetaModel = new PamelaMetaModel(WKFObject.class);
		factory = new PamelaModelFactory(pamelaMetaModel);

		final EditingContextImpl editingContext = new EditingContextImpl();
		editingContext.createUndoManager();
		factory.getModel().setEditingContext(editingContext);
		undoManager = editingContext.getUndoManager();
	}

	@Test
	public void testAttributesUndoRedo() throws Exception {
		final String defaultValue = (String) pamelaMetaModel.getModelEntity(TestModelObject.class).getModelProperty(TestModelObject.NAME)
				.getDefaultValue(factory);
		final CompoundEdit initial = undoManager.startRecording("initial");

		final StartNode node1 = factory.newInstance(StartNode.class);
		node1.init("0", defaultValue);
		final StartNode node2 = factory.newInstance(StartNode.class);
		node2.init("1", defaultValue);
		undoManager.stopRecording(initial);

		// simple set
		assertEquals(defaultValue, node1.getName());
		CompoundEdit edit = undoManager.startRecording("testSimpleAttributesUndo1");
		node1.setName("start");
		undoManager.stopRecording(edit);
		assertEquals("start", node1.getName());
		undoManager.undo();
		assertEquals(defaultValue, node1.getName());
		undoManager.redo();
		assertEquals("start", node1.getName());
		undoManager.undo();

		// multiple sets one object
		assertEquals(defaultValue, node1.getName());
		edit = undoManager.startRecording("testSimpleAttributesUndo2");
		node1.setName("start1");
		assertEquals("start1", node1.getName());
		node1.setName("start2");
		assertEquals("start2", node1.getName());
		node1.setName("start3");
		undoManager.stopRecording(edit);
		assertEquals("start3", node1.getName());
		undoManager.undo();
		assertEquals(defaultValue, node1.getName());
		undoManager.redo();
		assertEquals("start3", node1.getName());
		undoManager.undo();

		// multiple sets two object
		assertEquals(defaultValue, node1.getName());
		assertEquals(defaultValue, node2.getName());
		edit = undoManager.startRecording("testSimpleAttributesUndo3");
		node1.setName("original");
		assertEquals("original", node1.getName());
		node2.setName("toto");
		assertEquals("toto", node2.getName());
		undoManager.stopRecording(edit);
		undoManager.undo();
		assertEquals(defaultValue, node1.getName());
		assertEquals(defaultValue, node2.getName());
		undoManager.redo();
		assertEquals("original", node1.getName());
		assertEquals("toto", node2.getName());
		// keeps them to original and toto

		// sets to an original value
		edit = undoManager.startRecording("testSimpleAttributesUndo4");
		node1.setName("toto");
		assertEquals("toto", node1.getName());
		undoManager.stopRecording(edit);
		undoManager.undo();
		assertEquals("original", node1.getName());
		undoManager.redo();
		assertEquals("toto", node1.getName());
		undoManager.undo();
		assertEquals("original", node1.getName());
		undoManager.undo();

		// undo initial
		undoManager.undo();
		assertEquals(false, undoManager.canUndo());
		assertEquals(true, undoManager.canRedo());
	}

	@Test
	public void testSimpleCollectionUndo() throws Exception {
		final CompoundEdit initial = undoManager.startRecording("initial");

		final FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("root");

		final StartNode node1 = factory.newInstance(StartNode.class);
		node1.init("1", "node1");

		final EndNode node2 = factory.newInstance(EndNode.class);
		node2.init("2", "node2");

		final TokenEdge edge1 = factory.newInstance(TokenEdge.class);
		edge1.init("3");

		final TokenEdge edge2 = factory.newInstance(TokenEdge.class);
		edge2.init("4");

		undoManager.stopRecording(initial);

		CompoundEdit edit = undoManager.startRecording("testSimpleCollectionUndo1");
		node1.addToOutgoingEdges(edge1);
		node2.addToIncomingEdges(edge1);

		node1.addToIncomingEdges(edge2);
		node2.addToOutgoingEdges(edge2);

		assertEquals(1, node1.getIncomingEdges().size());
		assertEquals(1, node2.getIncomingEdges().size());
		assertEquals(1, node1.getOutgoingEdges().size());
		assertEquals(1, node1.getOutgoingEdges().size());

		process.addToNodes(node1);
		process.addToNodes(node2);
		assertEquals(2, process.getNodes().size());

		undoManager.stopRecording(edit);

		undoManager.undo();
		assertEquals(0, node1.getIncomingEdges().size());
		assertEquals(0, node2.getIncomingEdges().size());
		assertEquals(0, node1.getOutgoingEdges().size());
		assertEquals(0, node1.getOutgoingEdges().size());
		assertEquals(0, process.getNodes().size());

		undoManager.redo();
		assertEquals(1, node1.getIncomingEdges().size());
		assertEquals(1, node2.getIncomingEdges().size());
		assertEquals(1, node1.getOutgoingEdges().size());
		assertEquals(1, node1.getOutgoingEdges().size());
		assertEquals(2, process.getNodes().size());

		undoManager.undo();
		assertEquals(0, node1.getIncomingEdges().size());
		assertEquals(0, node2.getIncomingEdges().size());
		assertEquals(0, node1.getOutgoingEdges().size());
		assertEquals(0, node1.getOutgoingEdges().size());
		assertEquals(0, process.getNodes().size());

		// undo initial
		undoManager.undo();
		assertEquals(false, undoManager.canUndo());
		assertEquals(true, undoManager.canRedo());
	}

	@Test
	public void testMultipleUndoRedo() throws Exception {
		final CompoundEdit initial = undoManager.startRecording("initial");
		final FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("root");

		final int step = 3;
		final int count = step * 10;
		final StartNode[] nodes = new StartNode[count];
		for (int i = 0; i < nodes.length; i++) {
			final StartNode node = factory.newInstance(StartNode.class);
			node.init("node" + i, "node" + i);
			nodes[i] = node;
		}
		undoManager.stopRecording(initial);

		assertEquals(0, process.getNodes().size());

		// adds all nodes by step size
		for (int i = 0; i < count; i += step) {
			final CompoundEdit edit = undoManager.startRecording("testMultipleUndoRedo" + i / step);
			for (int j = 0; j < step; j++) {
				process.addToNodes(nodes[i + j]);
			}
			undoManager.stopRecording(edit);
		}
		assertEquals(count, process.getNodes().size());

		assertEquals(true, undoManager.canUndo());
		assertEquals(false, undoManager.canRedo());

		// tests several time undo all, redo all.
		for (int k = 0; k < 5; k += 1) {
			// undo all one by one
			for (int i = 0; i < count; i += step) {
				undoManager.undo();
				assertEquals(count - i - step, process.getNodes().size());
			}

			assertEquals(true, undoManager.canRedo());

			// redo all one by one
			for (int i = 0; i < count; i += step) {
				undoManager.redo();
				assertEquals(i + step, process.getNodes().size());
			}
		}

		// undo all one by one (again)
		for (int i = 0; i < count; i += step) {
			undoManager.undo();
			assertEquals(count - i - step, process.getNodes().size());
		}

		// undo initial
		undoManager.undo();
		assertEquals(false, undoManager.canUndo());
		assertEquals(true, undoManager.canRedo());
	}

	@Test
	public void testAddUndoRedo() throws Exception {
		final CompoundEdit initial = undoManager.startRecording("initial");
		final FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init("root");
		undoManager.stopRecording(initial);

		assertEquals(0, process.getNodes().size());

		// creates nodes by step size
		final int step = 3;
		final int count = step * 10;
		for (int i = 0; i < count; i += step) {
			final CompoundEdit edit = undoManager.startRecording("testAddUndoRedo" + i / step);
			for (int j = 0; j < step; j++) {
				final StartNode node = factory.newInstance(StartNode.class);
				node.init("node" + i, "node" + i);
				process.addToNodes(node);
			}
			undoManager.stopRecording(edit);
		}
		assertEquals(count, process.getNodes().size());

		assertEquals(true, undoManager.canUndo());
		assertEquals(false, undoManager.canRedo());

		// tests several time undo all, redo all.
		for (int k = 0; k < 5; k += 1) {
			// undo all one by one
			for (int i = 0; i < count; i += step) {
				undoManager.undo();
				assertEquals(count - i - step, process.getNodes().size());
			}

			assertEquals(true, undoManager.canRedo());

			// redo all one by one
			for (int i = 0; i < count; i += step) {
				undoManager.redo();
				assertEquals(i + step, process.getNodes().size());
			}
		}

		// undo all one by one (again)
		for (int i = 0; i < count; i += step) {
			undoManager.undo();
			assertEquals(count - i - step, process.getNodes().size());
		}

		// undo initial
		undoManager.undo();
		assertEquals(false, undoManager.canUndo());
		assertEquals(true, undoManager.canRedo());
	}

	@Test
	public void testRemoveUndoRedo() {
		final CompoundEdit initial = undoManager.startRecording("initial");
		final FlexoProcess process = factory.newInstance(FlexoProcess.class);
		process.init();

		// creates nodes
		final int step = 3;
		final int count = step * 10;
		for (int i = 0; i < count; i += 1) {
			final StartNode node = factory.newInstance(StartNode.class);
			node.init("node" + i);
			process.addToNodes(node);
		}
		assertEquals(count, process.getNodes().size());
		undoManager.stopRecording(initial);

		// deletes nodes by step size
		for (int i = 0; i < count; i += step) {
			final CompoundEdit edit = undoManager.startRecording("testRemoveUndoRedo" + i / step);
			for (int j = 0; j < step; j++) {
				AbstractNode node = process.getNodeNamed("node" + (i + j));
				process.removeFromNodes(node);
			}
			undoManager.stopRecording(edit);
		}
		assertEquals(0, process.getNodes().size());

		assertEquals(true, undoManager.canUndo());
		assertEquals(false, undoManager.canRedo());

		// tests several time undo all, redo all.
		for (int k = 0; k < 5; k += 1) {
			// undo all one by one
			for (int i = 0; i < count; i += step) {
				undoManager.undo();
				assertEquals(i + step, process.getNodes().size());
			}

			assertEquals(true, undoManager.canRedo());

			// redo all one by one
			for (int i = 0; i < count; i += step) {
				undoManager.redo();
				assertEquals(count - i - step, process.getNodes().size());
			}
		}

		// undo all one by one (again)
		for (int i = 0; i < count; i += step) {
			undoManager.undo();
			assertEquals(i + step, process.getNodes().size());
		}

		// undo initial
		undoManager.undo();
		assertEquals(false, undoManager.canUndo());
		assertEquals(true, undoManager.canRedo());
	}
}
