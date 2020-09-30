# Clipboard operations

PAMELA framework offers a native support for clipboard operations. Model should be annotated with `@PastingPoint` annotations, defined on *ModelProperty*. This indicates that this property may receive the "pasting" of the contents of a given clipboard, if the type of data in clipboard is assignable to the related property (either a single object or a set of object with a compatible type).

Available API to perform clipboard operations is exposed by following methods defined in *ModelFactory* class:

- `copy(Object... objects)` : return a `Clipboard` object containing a clone of supplied objects
- `cut(Object... objects)` : return a `Clipboard` object containing a clone of supplied objects, and delete original objects
- `isPastable(Clipboard,Object)` : return a flag indicating if pasting operation is possible for supplied clipboard into target object
- `paste(Clipboard,Object)` : set (or add depending on property cardinality) objects in supplied clipboard into target object (this method assumes that pasting point should be non ambigous: a unique property may receive this type of object)
- `paste(Clipboard,ModelProperty,Object)` : non-ambigous version of previous method


Example of code with a "node" property declared as receiver for copying a list of `Node`
 
```java
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
```

    
  
