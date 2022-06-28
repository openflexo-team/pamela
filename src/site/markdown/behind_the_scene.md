# Behind the scene

## Runtime considerations

The aforementioned models are executed at runtime as a combination of two components:

1. plain Java byte-code, as the result of the basic compilation of source code; and
2.	 an embedded PAMELA interpreter, executing semantics reflected by *ModelEntity* and *ModelProperty*  declarations (together with custom annotations where available).

The main idea of the approach is to override the Java dynamic binding (the scheme used to bind method call to method body, which is not done by the compiler but at runtime). Invoking a method on an object which is part of a PAMELA model, causes the real implementation to be called when existing (more precisely dispatch code execution between all provided implementations), or the required interpretation according to underlying model to be executed.

The PAMELA interpreter intercepts any method call to an instance of a *ModelEntity* and conditionally branches code execution.

- If the accessed method is part of a *ModelProperty* (a getter, or a setter, etc..), and no custom implementation is defined neither in the class declared as implementation, nor in a class declared as partial implementation in the context of traits, then execution is delegated to the related property implementation (a generic code provided by the PAMELA interpreter).
- If the accessed method is defined in a class declared as implementation, or in a class declared as partial implementation, then this method is executed (with a scheme preventing method declaration clashes). The PAMELA API through the `AccessibleProxyObject` interface also provides access to the generic behaviour (super implementation), allowing the developer to define an overriding composition.

This general scheme provides also an extension point allowing to instrument the code, which is used for other features such as notification management, undo/redo stack management, assertion checking at runtime (support for *Design by Contract*, as in JML, and dynamic code weaving in the context of *Aspect Programming*).

This composition of an interpreter (interpreting both standard and specific semantics) and compiled code offers many benefits:

- Strong coupling between model and code 
- Strong typing is kept, and required checks are performed by the Java compiler
- The PAMELA framework provides interpretation of model@runtime
- No need to generate POJO (plain old Java objects), as their execution follow the standard semantics (less code, less bugs)
- Custom implementation are provided if needed, using usual Java extension points
- It offers a way to intercept method calls and instrument the code
- Assertions checking at runtime
- Dynamic code weaving at runtime (aspect programming without compilation)

[//]: # (@Sylvain, uniformise la forme des points certains sont des phrases et pas d'autres)

## Exposed API at design time

The model-code integration we advocate requires facilities to encode metadata in source code. This requires an annotation-enabled language. Such a language supports the attribute-oriented programming if its grammar allows adding custom declarative tags to annotate standard program elements. Java programming language from version 1.5 is a good candidate with the support of annotations.

[//]: # (@Sylvain, déjà lu le paragraphe précédent)

API exposed to the developer mainly consists of:

1. a set of annotations;
2. a set of unimplemented Java interfaces exposing the required features.

The package `org.openflexo.pamela.annotations` package exposes the set of annotations which were presented above.

The package `org.openflexo.pamela` contains following feature-related Java interfaces:

- `AccessibleProxyObject` is the interface that PAMELA objects should extend in order to benefit from base features such as generic default implementation, containment management, notification, object graph comparison and diff/merge, visiting patterns, etc.
- `CloneableProxyObject` exposes features related to cloning.
- `DeletableProxyObject` exposes features related to deletion management.
- `SpecifiableProxyObject` exposes dynamic assertion checking features in the context of JML (contract management) use.

Generic design patterns API, used in the context of aspect programming is exposed in the `org.openflexo.pamela.patterns` package. A plug-in architecture allows to enrich model with specific design patterns. Some basic design patterns are released with PAMELA 1.6.x in the context of security (`Authenticator`, `Authorization`, `SingleAccessPoint`, `Owner`).

## The PAMELA interpreter

The package `org.openflexo.pamela.factory` contains PAMELA interpreter implementation. The core of this interpreter is implemented in the class `ProxyMethodHandler`.

From a technical point of view, PAMELA implementation uses *javassist* reflection library, providing `MethodHandler` mechanism, which is a way to override the Java dynamic binding. Invoking a method on an object which is part of a PAMELA model, causes the real implementation to be called when existing (more precisely dispatch code execution between all provided implementations), or the required interpretation according to underlying model to be executed. This provides also an extension point allowing to instrument the code, which is used for other features such as undo/redo stack management, and assertion checking at run-time (support for Design by Contract, aka JML).

[//]: # (@Sylvain, déjà lu la fin du paragraphe précédent)

The PAMELA framework is 100% pure Java (> 1.5), compilable by a standard Java compiler and
executable in a standard Java Virtual Machine.
