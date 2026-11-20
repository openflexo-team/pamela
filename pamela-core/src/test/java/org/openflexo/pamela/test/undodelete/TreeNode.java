package org.openflexo.pamela.test.undodelete;

import java.util.List;

import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

/**
 * A node of a tree whose children are embedded: used to test the undo of a deletion (PAMELA-D-8).
 */
@ModelEntity
public interface TreeNode extends DeletableProxyObject {

	String NAME = "name";
	String PARENT = "parent";
	String CHILDREN = "children";

	@Getter(NAME)
	String getName();

	@Setter(NAME)
	void setName(String name);

	@Getter(value = PARENT, inverse = CHILDREN)
	TreeNode getParent();

	@Setter(PARENT)
	void setParent(TreeNode parent);

	@Getter(value = CHILDREN, cardinality = Cardinality.LIST, inverse = PARENT)
	@Embedded
	List<TreeNode> getChildren();

	@Setter(CHILDREN)
	void setChildren(List<TreeNode> children);

	@Adder(CHILDREN)
	void addToChildren(TreeNode child);

	@Remover(CHILDREN)
	void removeFromChildren(TreeNode child);
}
