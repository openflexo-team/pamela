/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.model4;

import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.DeserializationFinalizer;
import org.openflexo.pamela.annotations.DeserializationInitializer;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.factory.AccessibleProxyObject;

@ModelEntity
@ImplementationClass(Node.NodeImpl.class)
@XMLElement
public interface Node extends AccessibleProxyObject {

	public static final String NAME = "name";
	public static final String PARENT_NODE = "parent";
	public static final String NODES = "nodes";

	@Getter(value = NAME, defaultValue = "???")
	@XMLAttribute(xmlTag = NAME)
	public String getName();

	@Setter(NAME)
	public void setName(String name);

	@Getter(value = PARENT_NODE, inverse = NODES)
	public Node getParentNode();

	@Setter(PARENT_NODE)
	public void setParentNode(Node aNode);

	@Getter(value = NODES, cardinality = Cardinality.LIST, inverse = PARENT_NODE)
	@XMLElement(primary = true)
	@Embedded
	public List<Node> getNodes();

	@Setter(NODES)
	public void setNodes(List<Node> nodes);

	@Adder(NODES)
	public void addToNodes(Node node);

	@Remover(NODES)
	public void removeFromNodes(Node node);

	@DeserializationInitializer
	public void initializeDeserialization();

	@DeserializationFinalizer
	public void finalizeDeserialization();

	public static abstract class NodeImpl implements Node {

		public static String DESERIALIZATION_TRACE = "";

		private boolean isDeserializing = false;

		@Override
		public void initializeDeserialization() {
			System.out.println("Init deserialization for Node " + getName());
			isDeserializing = true;
		}

		@Override
		public void setName(String name) {
			if (isDeserializing) {
				DESERIALIZATION_TRACE += " BEGIN:" + name;
			}
			performSuperSetter(NAME, name);
		}

		@Override
		public void finalizeDeserialization() {
			isDeserializing = false;
			DESERIALIZATION_TRACE += " END:" + getName();
			System.out.println("Finalize deserialization for Node " + getName());
		}
	}

}
