# Notification support

Without any intervention of the developper, PAMELA framework offers a fine-tuned notification support. This notification scheme relies on the execution of write-access *ModelProperties* (properties using `set:`, `add:`, `add:AtIndex:`, `remove:`, `delete:` and `undelete:` protocols). Since the execution of *ModelProperty*-related methods is performed by embedded PAMELA interpreter, a default notification scheme is executed. 

Default implementation relies on `java.beans.PropertyChangeSupport` API (the `AccessibleProxyObject` API provided in PAMELA framework extends `@HasPropertyChangeSupport` Java interface which exposes `getPropertyChangeSupport()` method). Default implementation of notification scheme propagates a `PropertyChangeEvent` for each property changing its value, with the former and the new value.

The developper is responsible for the custom implementations which might be defined in its model. The use `performSuperSetter(String,Object)`, `performSuperAdder(String,Object)` or `performSuperRemover(String,Object)` may be usefull in the context of property custom redefinition. 

An interesting point is that this notification scheme may be easily extended by defining a new `PropertyImplementation` (see section on meta-programming). The developper does not have to redefine it for all properties, since all required properties should define a `@PropertyImplementation` annotation.


 

    
  
