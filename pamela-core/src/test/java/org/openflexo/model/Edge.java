package org.openflexo.model;

import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Initializer;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.Parameter;
import org.openflexo.model.annotations.ReturnedValue;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.impl.EdgeImpl;

@ModelEntity(isAbstract = true)
@ImplementationClass(EdgeImpl.class)
public interface Edge extends WKFObject {

	public static final String START_NODE = "startNode";
	public static final String END_NODE = "endNode";

	@Initializer
	public Edge init(@Parameter(START_NODE) AbstractNode start, @Parameter(END_NODE) AbstractNode end);

	@Initializer
	public Edge init(@Parameter(TestModelObject.NAME) String name, @Parameter(START_NODE) AbstractNode start,
			@Parameter(END_NODE) AbstractNode end);

	@Override
	@Getter(PROCESS)
	@ReturnedValue("startNode.process")
	public FlexoProcess getProcess();

	@Getter(value = START_NODE, inverse = AbstractNode.OUTGOING_EDGES)
	@XMLElement(context = "Start")
	@CloningStrategy(StrategyType.IGNORE)
	public AbstractNode getStartNode();

	@Setter(START_NODE)
	public void setStartNode(AbstractNode node);

	@Getter(value = END_NODE, inverse = AbstractNode.INCOMING_EDGES)
	@XMLElement(context = "End")
	@CloningStrategy(StrategyType.IGNORE)
	public AbstractNode getEndNode();

	@Setter(END_NODE)
	public void setEndNode(AbstractNode node);
}
