# Cloning support

PAMELA framework offers cloning features, and support is provided for many cloning strategies. 

In graph theory and combinatorial optimization, a closure of a directed graph is a set of vertices with no outgoing edges. That is, the graph should have no edges that start within the closure and end outside the closure. PAMELA frameworks relies on a internal scheme which computes the closure of an input graph infered from a explicit set of objects, considered as vertices, and following containment relationships, considered as edges. This containment closure computation is especially used in cloning support. 

Cloning strategy might be locally described in a *ModelProperty* using `@CloningStrategy` annotation. Available strategies are:

- `CLONE` : object(s) addressed by this property should be cloned as well, and also contained objects of addressed object(s)
- `REFERENCE` : object(s) addressed by this property should only be referenced
- `IGNORE` : object(s) addressed by this property should be ignored and cloned object should address nullified properties
- `FACTORY` and `CUSTOM_CLONE` are two additional advanced options for the developer to define custom code to handle custom cloning

Cloning might be independently performed (use of `cloneObject()` method) or in the context of a subset of an object graph (use of `cloneObject(Object... context}`, where context represents a set of objects which have to be cloned together). Cloning of an object graph strongly relies on containment closure computation since references to objects should be set according to the cloned object graph.

 

    
  
