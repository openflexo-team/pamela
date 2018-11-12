package org.flexo.model;

import org.flexo.model.impl.EdgeImpl;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.ReturnedValue;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;

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
