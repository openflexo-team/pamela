---
sidebar_position: 10
---

# Persistance support, XML serialization/deserialization

In most applications, persistency is generally required to guarantee the recovery and communication of structured data along time and applications.

While PAMELA framework is agnostic from a serialization format, today only `XML` serialization is provided, but other serialization formats are beeing considered, such as `JSON`.

Support for XML serialization in PAMELA requires that underlying model is annotated using those two annotations `@XMLElement` and `@XMLAttribute`.

- `@XMLElement` : This annotation should be defined either on a *ModelEntity* or on a *ModelProperty* (in this case with an eventual contextual prefix to avoid ambiguities between properties addressing this type).
- `@XMLAttribute`: This annotation should be defined on a *ModelProperty*

Serialization scheme relies on a String serializer/deserializer for common types, and allows alternatives types custom serializer/deserializer, which should be defined as add-ons.

The encoding of the object graph structure (and not only trees, as reflected by XML structure) relies on references (use of `idref` attributes). The serialization strategy is highly configurable, and allows a persistent-stable structure using a `primary` feature defined on some *ModelProperties* (referenced object will be preferably extensively serialized as this location).

Serialization and deserialization processes are provided with extension points (serialization/deserialization initializers/deserializers), where the developer may inject some specific code.

Excerpt of code showing XML serialization directives, and exposing deserialization extension points.

```java
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
```java

 

    
  
