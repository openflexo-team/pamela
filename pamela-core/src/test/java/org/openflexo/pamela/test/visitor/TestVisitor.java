package org.openflexo.pamela.test.visitor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.PamelaVisitor;
import org.openflexo.pamela.test.updatewith.ConceptA;
import org.openflexo.pamela.test.updatewith.ConceptB;
import org.openflexo.pamela.test.updatewith.ConceptC;
import org.openflexo.pamela.test.updatewith.ConceptC1;
import org.openflexo.pamela.test.updatewith.ConceptC2;

/**
 * Test PAMELA visitor feature
 * 
 * @author sylvain
 * 
 */
public class TestVisitor implements PamelaVisitor {

	private List<Object> visitedObjects = new ArrayList<Object>();

	@Override
	public void visit(Object object) {
		System.out.println("Visit: " + object);
		visitedObjects.add(object);
	}

	@Test
	public void testEmbeddingVisitor() throws Exception {

		PamelaMetaModel pamelaMetaModel = PamelaMetaModelLibrary.retrieveMetaModel(ConceptA.class, ConceptC1.class, ConceptC2.class);
		PamelaModelFactory factory = new PamelaModelFactory(pamelaMetaModel);

		ConceptA a1 = factory.newInstance(ConceptA.class);
		ConceptB b1 = factory.newInstance(ConceptB.class);
		a1.setValue("Hello");
		b1.setU1("Bonjour");
		b1.setU2("Le monde !");
		a1.setConceptB(b1);
		ConceptC c11 = factory.newInstance(ConceptC.class);
		ConceptC c12 = factory.newInstance(ConceptC.class);
		ConceptC c13 = factory.newInstance(ConceptC.class);
		c11.setV1("Riri");
		c11.setV2("Fifi");
		c11.setV3("Loulou");
		c12.setV1("Tom");
		c12.setV2("Jerry");
		c13.setV1("XTof");
		c13.setV2("Julien");
		c13.setV3("Sylvain");
		a1.addToConceptCs(c11);
		a1.addToConceptCs(c12);
		a1.addToConceptCs(c13);

		System.out.println(factory.stringRepresentation(a1));

		a1.accept(this);

		assertEquals(5, visitedObjects.size());
	}

	@Test
	public void testEmbeddingVisitor2() throws Exception {

		PamelaMetaModel pamelaMetaModel = new PamelaMetaModel(Graph.class);
		PamelaModelFactory factory = new PamelaModelFactory(pamelaMetaModel);

		Graph graph = factory.newInstance(Graph.class);

		Node node1 = factory.newInstance(Node.class);
		node1.setName("Node1");
		graph.addToNodes(node1);

		Node node2 = factory.newInstance(Node.class);
		node2.setName("Node2");
		graph.addToNodes(node2);

		Node node3 = factory.newInstance(Node.class);
		node3.setName("Node3");
		graph.addToNodes(node3);

		Edge edge1 = factory.newInstance(Edge.class).init(node1, node2);
		Edge edge2 = factory.newInstance(Edge.class).init(node2, node3);

		System.out.println(factory.stringRepresentation(graph));

		graph.accept(this, VisitingStrategy.Embedding);
		assertEquals(4, visitedObjects.size());

		visitedObjects.clear();
		graph.accept(this, VisitingStrategy.Exhaustive);
		assertEquals(6, visitedObjects.size());
	}

}
