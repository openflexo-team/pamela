package org.openflexo.model9;

import java.util.List;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@XMLElement
public interface Graph extends AccessibleProxyObject {

	String NODES = "nodes";

	@Getter(value = NODES, cardinality = Cardinality.LIST, inverse = Node.GRAPH)
	@XMLElement(primary = true)
	@Embedded
	List<Node> getNodes();

	@Adder(NODES)
	@PastingPoint
	void addToNodes(Node node);

	@Remover(NODES)
	void removeFromNodes(Node node);

}
