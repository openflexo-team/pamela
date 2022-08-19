---
sidebar_position: 12
---

# Visiting features

Visitor pattern is offered by `AccessibleProxyObject` base API, with the two methods `accept(PAMELAVisitor)` and `accept(PAMELAVisitor,VisitingStrategy)`. 

The visiting strategy should be one of `Embedding` or `Exhaustive` (default strategy is `Embedding`), where `PAMELAVisitor` is a trivial Java interface presenting the method `visit(Object(Object)`. 

- `Embedding` visiting strategy results in a deep-first tree exploration where the tree is the covering tree supported by embedding properties. 
- `Exhaustive` visiting strategy result in a full exploration of the object graph.

Here is the API of visitor pattern:

```java
public interface AccessibleProxyObject extends HasPropertyChangeSupport, KeyValueCoding {

    ...

	/**
	 * Called to be visited by a {@link PAMELAVisitor}
	 * 
	 * Default strategy is Embedding
	 * 
	 * @param visitor
	 */
	public void accept(PAMELAVisitor visitor);

	/**
	 * Called to be visited by a {@link PAMELAVisitor}
	 * 
	 * @param visitor
	 * @param strategy
	 */
	public void accept(PAMELAVisitor visitor, VisitingStrategy strategy);

    ...
}
```

With a visitor declared as a class implementing:

```java
public interface PAMELAVisitor {

	public void visit(Object object);

	public enum VisitingStrategy {
		Embedding, Exhaustive
	}
}
```




 

    
  
