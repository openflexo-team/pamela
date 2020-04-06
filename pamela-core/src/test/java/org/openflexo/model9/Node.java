package org.openflexo.model9;

import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.factory.AccessibleProxyObject;

@ModelEntity
@XMLElement
public interface Node extends AccessibleProxyObject {

	public static final String NAME = "name";
	public static final String OUTGOING_EDGES = "outgoingEdges";
	public static final String INCOMING_EDGES = "incomingEdges";

	public static final String GRAPH = "graph";

	@Getter(GRAPH)
	@Embedded
	public Graph getGraph();

	@Setter(GRAPH)
	public void setGraph(Graph aGraph);

	@Getter(value = NAME, defaultValue = "???")
	@XMLAttribute
	public String getName();

	@Setter(NAME)
	public void setName(String name);

	@Getter(value = OUTGOING_EDGES, cardinality = Cardinality.LIST, inverse = Edge.START_NODE)
	@XMLElement(context = "Outgoing", primary = true)
	public List<Edge> getOutgoingEdges();

	@Setter(OUTGOING_EDGES)
	public void setOutgoingEdges(List<Edge> edges);

	@Adder(OUTGOING_EDGES)
	public void addToOutgoingEdges(Edge edge);

	@Remover(OUTGOING_EDGES)
	public void removeFromOutgoingEdges(Edge edge);

	@Getter(value = INCOMING_EDGES, cardinality = Cardinality.LIST, inverse = Edge.END_NODE)
	@XMLElement(context = "Incoming")
	public List<Edge> getIncomingEdges();

	@Setter(INCOMING_EDGES)
	public void setIncomingEdges(List<Edge> edges);

	@Adder(INCOMING_EDGES)
	public void addToIncomingEdges(Edge edge);

	@Remover(INCOMING_EDGES)
	public void removeFromIncomingEdges(Edge edge);

}
