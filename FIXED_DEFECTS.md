# pamela — Fixed defects

Defects of pamela that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

### PAMELA-D-2 — "Could not compute distance from a File for a non-file resource" is logged for a resource the converter addresses correctly  ·  `DONE`

**GitHub:** [openflexo-team/pamela#7](https://github.com/openflexo-team/pamela/issues/7)

**Symptom.** 62 warnings at the start of formod-app, on the AWT thread: `Could not compute distance from a File for a non-file resource: …formod-rc-2.99-SNAPSHOT.jar!/DomainModel/Icons/Variable.png` (38) and `Enumeration.png` (24). Every one comes from `RelativePathResourceConverter.computeRelativePath`, the only caller of `Resource.distance`.

**Verified.** A diagram has two container resources: its own directory, on disk (set by `DiagramFactory`), and the one of its diagram specification, in a jar (`DiagramResourceImpl.setMetaModelResource`). To serialize a resource of that jar the converter asks both for a distance and keeps the nearest. The one on disk cannot measure a resource of a jar: `FileResourceImpl.distance` warns and answers 1000, so the container of the jar was chosen, rightly. Only the warning was wrong (and the symmetrical `JarResourceImpl` / `InJarResourceImpl` one).

**Fixed (2026-10-13).** The converter no longer asks a container of the file system for the distance to a resource of a jar, nor the other way round: that distance is `Integer.MAX_VALUE`. `TestRelativePathResourceConverter.testContainersOfDifferentKinds` fails without the fix. Confirmed on a formod-app start with a Formose project opened and a domain diagram displayed (2026-10-13): 62 -> 0 `Could not compute distance` (the same project load, 32 `TestsFlexo` lines in the log, gave 62 before the fix). The start measured just before did not open any project, hence no conversion at all.

### PAMELA-D-3 — The synthetic `<Impl>_<Iface>_Proxy` class replaces the declared implementing class  ·  `DONE`

**GitHub:** [openflexo-team/pamela#8](https://github.com/openflexo-team/pamela/issues/8)

**Symptom.** When an entity's implementing class is not in the package of its interface, `getImplementingClassForInterface`, `ProxyMethodHandler.getSuperClass()` and `getOverridingSuperClass()` answer a generated class `<ifacePkg>.<Impl>_<Iface>_Proxy` instead of the declared one, as soon as one instance was created.

**Verified.** The Java 11 work-around of `PAMELAProxyFactory.newInstance` (PAMELA-30, 2020) inserts that empty subclass, defined in the interface's package, as the super class of the javassist proxy, through `setProxySuperClass`, which overwrites the super class the factory reports. `ImplementingClassTest.testImplementingClassInAnotherPackage` (interface `implementingclass.Item`, implementation `implementingclass.impl.ItemImpl` set by `setImplementingClassForInterface`) fails: expected `ItemImpl`, got `ItemImpl_Item_Proxy`.

**Consequences.** `XMLSerializer` writes `getOverridingSuperClass().getName()` as the `p:class` attribute, so an entity serialized with its model entity name (an entity not known to the factory's context) and an overridden implementation would carry the synthetic name, which does not exist in a fresh JVM. Not reproduced through serialization. Disappears with the Byte Buddy backend (PAMELA-F-1), which needs no intermediate class.

**Fixed (2026-10-16).** `PAMELAProxyFactory` no longer extends javassist's `ProxyFactory`: it keeps the declared implementing class itself and gives the intermediate class only to the javassist factory it holds, once per factory instead of at each instantiation. The generation of the intermediate class is now synchronized. `ImplementingClassTest` passes; pamela-core 75/75.

Since 2026-10-18 the intermediate class itself is gone with javassist (PAMELA-F-1): the proxy extends the declared implementing class directly.

### PAMELA-D-4 — With javassist, an implementation class loaded by a child class loader is replaced by another copy  ·  `DONE`

**GitHub:** [openflexo-team/pamela#9](https://github.com/openflexo-team/pamela/issues/9)

**Symptom.** An entity whose implementation class is loaded by a child class loader (child-first, as the jar class loader of a technology adapter), in another package than its interface, gets a proxy that does not extend that implementation class but another copy of it, loaded by the loader of the interface.

**Verified.** `ProxyBackendTest.testImplementationInAChildClassLoader[javassist]` (2026-10-18): `ChildItemImpl`, defined by a `ChildFirstLoader`, implementing `Item` of the parent loader. The intermediate class of the PAMELA-30 work-around is built from the name of the implementation class, resolved by `ClassPool.getDefault()` on the system class path, and defined next to the interface: its super class is whatever class of that name the interface's loader finds. When none is found, `NotFoundException` is printed and the intermediate class extends `Object`. Not observed in an application.

**Fix.** None for the javassist backend, which is to be removed (PAMELA-F-1): the Byte Buddy backend defines the proxy next to the implementation class when its loader sees the interface, and passes the same test.

**Closed (2026-10-18).** javassist and its work-around were removed from PAMELA 2.0 (PAMELA-F-1); `ProxyBackendTest.testImplementationInAChildClassLoader` passes. The defect comes back if the removal commit is reverted.

### PAMELA-D-5 — A model entity that fails to import breaks every later import  ·  `DONE`

**GitHub:** [openflexo-team/pamela#10](https://github.com/openflexo-team/pamela/issues/10)

**Symptom.** Once a `ModelDefinitionException` was raised while importing an entity (an inconsistent property, a primitive without
default value), every later `PamelaMetaModelLibrary.retrieveMetaModel` of unrelated entities failed with that same exception, in the
same JVM.

**Cause.** `ModelEntityLibrary.importEntity` registers the new entities, then merges their properties; an exception left them in the
library and in `newEntities`, never cleared, so the next import merged the broken entity again.

**Fixed (2026-11-10).** On failure the entities of that import are removed from the library, and `newEntities` is always cleared.
Shown by `TestPersistenceRule.testTransientCannotCarryAnXMLMapping` (an invalid entity on purpose): before the fix, 9 other tests of
pamela-core failed after it.

### PAMELA-D-6 — `ModelExecutionException` loses the cause it wraps, and prints its stack  ·  `DONE`

**GitHub:** [openflexo-team/pamela#11](https://github.com/openflexo-team/pamela/issues/11)

**Symptom (read, audit of `CORE-F-11`, openflexo-core, 2026-11-13).** `ModelExecutionException(Throwable)` (and the
`InvocationTargetException` constructor, which delegates to it) builds a message from the class name and the message of the cause, but
does not pass the cause to `RuntimeException`: `getCause()` is null and the original stack is lost to every caller. It and
`ModelExecutionException(String, Throwable)` also call `e.printStackTrace()`, so the failure is printed on stderr where it is created, before
anyone decides how to report it.

**Fixed (2026-11-14, `CORE-F-11` phase 2).** Every constructor passes the cause to `RuntimeException`, and none prints the stack. Test:
`TestModelExecutionException` (pamela-core).

### PAMELA-D-7 — TypeConverter does not read back the type void, so a concept with a void behaviour cannot be cloned or copied  ·  `DONE`

**GitHub:** [openflexo-team/pamela#16](https://github.com/openflexo-team/pamela/issues/16)

**Symptom (reproduced 2026-11-19, branch 2.0).** Copying a FlexoConcept in the FML editor of Openflexo 3.0 did nothing: `CopyAction.getClipboard()` was null, and `Openflexo.err.log` held `InvalidDataException: Supplied value represents a type not found: void` (`TypeConverter.convertFromString`, called from `CloningHandler.finalizeClone`). Any concept declaring a behaviour returning `void` failed to clone.

**Cause.** `TypeConverter.convertFromString` mapped the primitive names `boolean`, `byte`, `int`, `long`, `short`, `float`, `double` and `char` to their `TYPE`, but not `void`: it fell through to `Class.forName("void")`, which throws. Cloning re-reads a string-convertible `Type` property from its text.

**Fixed (2026-11-19).** `convertFromString` maps `void` to `Void.TYPE` (`convertToString(Void.TYPE)` already gave `void`). `TestTypeConverter.testPrimitiveTypesRoundTrip` checks the round trip of every primitive type and `void`; `TestTypeConverter` 8/8. The openflexo-core test `KnownDefectsCloneVoidBehaviourTest` (fml-parser) was not run: it needs the openflexo-dev composite.

### PAMELA-D-8 — Undoing a deletion leaves the restored objects deleted and their lists in reverse order  ·  `DONE`

**GitHub:** [openflexo-team/pamela#17](https://github.com/openflexo-team/pamela/issues/17)

**Symptom (reproduced 2026-11-19).** In the FML editor of Openflexo 3.0, deleting a concept that contains nested concepts and then
undoing the deletion leaves a wrong model. In the application, a nested concept was printed twice at the root of the VirtualModel. In a
test, the model is inconsistent after the undo:
- every restored object is still marked deleted (`isDeleted() == true`);
- the lists of the restored objects come back in reverse order: the nested concepts of `Root1` are `[Root2, Beta, Alpha]` instead of
  `[Alpha, Beta, Root2]`;
- the restored concept is appended at the end of its container's list instead of going back to its index.

**Verified.** The same scenario gives the same result before and after openflexo-core `CORE-F-19` (openflexo-core#132), which changed
the containment of concepts. Before that change, the flat list of all concepts also came back scrambled. So the cause is in the undo of
a deletion (`DeleteCommand`, `ObjectLifecycleHandler.undelete`, the `undelete()` of the property implementations), not in the FML
model. The exact symptom seen in the application (a concept printed twice) is not reproduced in a test: the application goes through an
action and through the FML editor's re-parse.

**How to reproduce.** `TestUndoDelete` (pamela-core) and `TestUndoDeleteConcept` (openflexo-core, fml-parser): they parse or build a tree of nested objects, record the deletion of the root in the undo manager, undo it, and check the state.

**Cause.** Two independent faults, all in the path of an undo:
- An implementation that overrides `delete()` (as `FlexoConceptImpl` does) reaches the deletion only through `performSuperDelete`, which
  did not record a `DeleteCommand`: the undo had no deletion to undo.
- `RemoveCommand` did not keep the index of the removed value, so its undo appended the value: lists came back in reverse order, and a
  restored object went to the end of its container's list.

**Fixed (2026-11-19).** `performSuperDelete` records the deletion; `RemoveCommand` keeps the index and its undo puts the value back there
(`reindex` of a list no longer requires a `@Reindexer` method); `ObjectLifecycleHandler.undelete` returns `true`. Tests: `TestUndoDelete` (pamela-core, undo and redo of the deletion of a tree of
embedded objects; pamela-core 95/95) and `TestUndoDeleteConcept` (openflexo-core, fml-parser; fml-parser, flexo-foundation-test and
fml-cli-test green). The symptom seen in the application (a concept printed twice) was not re-checked in the running FML editor.
