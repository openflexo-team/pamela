package org.openflexo.pamela.test.reparent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.test.undodelete.TreeNode;

/**
 * PAMELA-D-9: after an object moved to another parent by the adder of that parent (a single property with a list inverse),
 * detaching it or deleting it no longer reset the single property. A move through the setter was not affected. The re-entrant set(null), done by the remover of the old parent, left its entry in the scheduled sets, so any later
 * set(null) of that property was taken for a loop and ignored.
 */
public class TestReparentThenDetach {

	private PamelaModelFactory factory;

	@Before
	public void setUp() throws Exception {
		factory = new PamelaModelFactory(PamelaMetaModelLibrary.retrieveMetaModel(TreeNode.class));
	}

	private TreeNode node(String name) {
		TreeNode n = factory.newInstance(TreeNode.class);
		n.setName(name);
		return n;
	}

	@Test
	public void removingAMovedChildResetsItsParent() throws Exception {
		TreeNode a = node("a");
		TreeNode b = node("b");
		TreeNode c = node("c");
		a.addToChildren(c);
		c.setParent(b);
		assertSame(b, c.getParent());
		assertEquals(List.of(), a.getChildren());
		assertEquals(List.of(c), b.getChildren());

		b.removeFromChildren(c);
		assertEquals(List.of(), b.getChildren());
		assertNull(c.getParent());
	}

	@Test
	public void settingTheParentOfAMovedChildToNullDetachesIt() throws Exception {
		TreeNode a = node("a");
		TreeNode b = node("b");
		TreeNode c = node("c");
		a.addToChildren(c);
		b.addToChildren(c);
		assertSame(b, c.getParent());
		assertEquals(List.of(), a.getChildren());

		c.setParent(null);
		assertNull(c.getParent());
		assertEquals(List.of(), b.getChildren());
	}

	@Test
	public void removingAChildMovedByTheAdderResetsItsParent() throws Exception {
		TreeNode a = node("a");
		TreeNode b = node("b");
		TreeNode c = node("c");
		a.addToChildren(c);
		b.addToChildren(c);
		b.removeFromChildren(c);
		assertEquals(List.of(), b.getChildren());
		assertNull(c.getParent());
	}

	@Test
	public void deletingAChildMovedByTheAdderRemovesItFromItsParent() throws Exception {
		TreeNode a = node("a");
		TreeNode b = node("b");
		TreeNode c = node("c");
		a.addToChildren(c);
		b.addToChildren(c);
		c.delete();
		assertFalse(b.getChildren().contains(c));
		assertEquals(List.of(), a.getChildren());
	}

	@Test
	public void aChildCanMoveSeveralTimes() throws Exception {
		TreeNode a = node("a");
		TreeNode b = node("b");
		TreeNode d = node("d");
		TreeNode c = node("c");
		a.addToChildren(c);
		c.setParent(b);
		c.setParent(d);
		c.setParent(a);
		assertSame(a, c.getParent());
		assertEquals(List.of(c), a.getChildren());
		assertEquals(List.of(), b.getChildren());
		assertEquals(List.of(), d.getChildren());
		c.setParent(null);
		assertNull(c.getParent());
		assertEquals(List.of(), a.getChildren());
	}
}
