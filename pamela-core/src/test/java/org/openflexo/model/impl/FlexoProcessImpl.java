package org.openflexo.model.impl;

import org.openflexo.model.AbstractNode;
import org.openflexo.model.Edge;
import org.openflexo.model.FlexoProcess;

public abstract class FlexoProcessImpl extends FlexoModelObjectImpl implements FlexoProcess {

	@Override
	public String toString() {
		return "FlexoProcessImpl id=" + getFlexoID() + " name=" + getName() + " nodes=" + getNodes();
	}

	@Override
	public AbstractNode getNodeNamed(String name) {
		for (AbstractNode n : getNodes()) {
			if (n.getName() != null && n.getName().equals(name)) {
				return n;
			}
		}
		return null;
	}

	@Override
	public Edge getEdgeNamed(String name) {
		for (AbstractNode n : getNodes()) {
			for (Edge e : n.getIncomingEdges()) {
				if (e.getName() != null && e.getName().equals(name)) {
					return e;
				}
			}
			for (Edge e : n.getOutgoingEdges()) {
				if (e.getName() != null && e.getName().equals(name)) {
					return e;
				}
			}
		}
		return null;
	}

}
