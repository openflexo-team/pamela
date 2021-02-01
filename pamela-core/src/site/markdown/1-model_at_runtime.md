# Model at runtime computation


PAMELA model at runtime is computed dynamically, working on the classpath of launched java application, and starting from a simple java interface (or a collection of java interfaces) which is/are PAMELA-annotated. From a mathematical point of view, internal representation of the underlying model is a graph whose vertex are PAMELA *ModelEntities* (annotated java interface), and edges are either inheritance links or reference links (a property whose type is another *ModelEntity*). `@Imports` and `@Import` annotations allows to include some other *ModelEntities* in the model. On the contrary, an annotation attribute `@Getter(...ignoreType=true)` allows to ignore the link. In that context, PAMELA model computation is a graph closure computation, starting from a collection of vertices. 

A PAMELA model at runtime is represented by a `ModelContext`. PAMELA instances (instances of *ModelEntity*) are handled though the use of `ModelFactory`, which is instantiated from a `ModelContext`.

PAMELA model closure computation on-the-fly provides an interesting approach to deal with model fragmentation.

 

    
  
