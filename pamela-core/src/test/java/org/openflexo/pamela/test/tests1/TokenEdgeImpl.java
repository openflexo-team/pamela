package org.openflexo.pamela.test.tests1;

public abstract class TokenEdgeImpl extends EdgeImpl implements TokenEdge {

	// Show how internal scheme can be overriden in custom implementation
	@Override
	public void setStartNode(AbstractNode node) {
		// System.out.println("Overriding setStartNode() startNode was "+getStartNode());
		performSuperSetter(Edge.START_NODE, node);
		// System.out.println("Overrided setStartNode() startNode is now "+getStartNode());
	}

}
