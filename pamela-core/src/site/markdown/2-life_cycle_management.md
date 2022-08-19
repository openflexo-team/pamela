---
sidebar_position: 3
---

# PAMELA objects life-cycle management

A Metamodel computation is represented by a `ModelContext` and uses a `ModelFactory` built with that model context to handle instances of that metamodel. The `ModelFactory` is responsible of the life-cycle of instances of metamodel (construction and destruction of Java instances). `@Initializer` annotation allows to define parametered constructors for *ModelEntity* instances.

`AccessibleProxyObject` is a Java interface providing utilities methods which are interpreted by internal PAMELA interpreter. This includes calls to internal code execution, such as `performSuperSetter(String,Object)` which represent a call to internal setter of property identified by supplied String value.

Following figure illustrates life-cycle of objects beeing instantiated as PAMELA instances. The `ModelFactory` initiates creation and triggers right constructor during a phase where the object is in `isCreating` status.

![LifeCycle](https://support.openflexo.org/images/components/pamela/LifeCycle.png)

Modifications of objects are internally tracked by PAMELA interpreter which manages `modified` status, according to containment semantics as presented further (a contained object modification implies object flagged as modified, and implied container flagged as modified too). Saving object graph brings back object status in `Alive/Saved` status.

Since calls to any features of model objects are dispatched by the internal interpreter, PAMELA runtime offers a multi-level undo/redo stack tooling. When enabled, this scheme allows to store and manage an edition model composed of atomic edits. Following figure presents atomic edits metamodel for a fine-grained model modification tracking system. That mechanism provides undo/redo features, virtually unlimited. For performance reasons, we can set a maximum depth for undo/redo operations.

![AtomicEditMetaModel](https://support.openflexo.org/images/components/pamela/AtomicEditMetaModel.png)

`DeletableProxyObject` provides delete (and undelete) features to *ModelEntity* instances. Deletion features are performed using a context which is a graph closure computation for all the objects which have to be deleted. PAMELA also offers undelete feature which allow to resurrect a deleted object. Deleted objects are still resurectable until they are still in the undo/redo stack. A deleted object who is leaving scope of maximum depth of undo/redo stack is destroyed. Object is fully dereferenced, ready for garbage collecting, and cannot be resurrected again.


    
  
