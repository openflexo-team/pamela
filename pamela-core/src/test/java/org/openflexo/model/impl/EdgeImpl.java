package org.openflexo.model.impl;

import org.openflexo.model.Edge;

public abstract class EdgeImpl extends FlexoModelObjectImpl implements Edge {

	public EdgeImpl() {
		super();
		// (new Exception("Build EdgeImpl "+Integer.toHexString(hashCode()))).printStackTrace();
	}

	@Override
	public String toString() {
		return "EdgeImpl " + getName() + " id=" + getFlexoID() + " startNode=" + getStartNode() + " endNode=" + getEndNode();
	}

}
