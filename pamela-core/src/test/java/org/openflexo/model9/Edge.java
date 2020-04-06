package org.openflexo.model9;

import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.factory.AccessibleProxyObject;

@ModelEntity
@XMLElement
public interface Edge extends AccessibleProxyObject {

	public static final String START_NODE = "startNode";
	public static final String END_NODE = "endNode";

	@Initializer
	public Edge init(@Parameter(START_NODE) Node start, @Parameter(END_NODE) Node end);

	@Getter(value = START_NODE, inverse = Node.OUTGOING_EDGES)
	@XMLElement(context = "Start")
	public Node getStartNode();

	@Setter(START_NODE)
	public void setStartNode(Node node);

	@Getter(value = END_NODE, inverse = Node.INCOMING_EDGES)
	@XMLElement(context = "End")
	public Node getEndNode();

	@Setter(END_NODE)
	public void setEndNode(Node node);
}
