package org.openflexo.pamela.test.tests1;

import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Finder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(FlexoProcessImpl.class)
@XMLElement(xmlTag = "FlexoProcess")
@Imports({ @Import(ActivityNode.class), @Import(StartNode.class), @Import(EndNode.class), @Import(TokenEdge.class),
		@Import(WKFAnnotation.class) })
public interface FlexoProcess extends WKFObject {

	String FOO = "foo";
	String NODES = "nodes";

	@Override
	@Initializer
	TestModelObject init(String flexoId);

	@Getter(value = FOO, defaultValue = "4")
	@XMLAttribute(xmlTag = FOO)
	int getFoo();

	@Setter(FOO)
	void setFoo(int foo);

	@Getter(value = NODES, cardinality = Cardinality.LIST, inverse = WKFObject.PROCESS)
	@XMLElement(primary = true)
	@CloningStrategy(StrategyType.CLONE)
	@Embedded
	List<AbstractNode> getNodes();

	@Setter(NODES)
	void setNodes(List<AbstractNode> nodes);

	@Adder(NODES)
	@PastingPoint
	void addToNodes(AbstractNode node);

	@Remover(NODES)
	void removeFromNodes(AbstractNode node);

	@Finder(attribute = AbstractNode.NAME, collection = NODES)
	AbstractNode getNodeNamed(String name);

	@Finder(attribute = AbstractNode.NAME, collection = NODES, isMultiValued = true)
	List<AbstractNode> getNodesNamed(String name);

	Edge getEdgeNamed(String name);

}
