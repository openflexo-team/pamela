# pamela — Fixed defects

Defects of pamela that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

### PAMELA-D-2 — "Could not compute distance from a File for a non-file resource" is logged for a resource the converter addresses correctly  ·  `DONE`

**Symptom.** 62 warnings at the start of formod-app, on the AWT thread: `Could not compute distance from a File for a non-file resource: …formod-rc-2.99-SNAPSHOT.jar!/DomainModel/Icons/Variable.png` (38) and `Enumeration.png` (24). Every one comes from `RelativePathResourceConverter.computeRelativePath`, the only caller of `Resource.distance`.

**Verified.** A diagram has two container resources: its own directory, on disk (set by `DiagramFactory`), and the one of its diagram specification, in a jar (`DiagramResourceImpl.setMetaModelResource`). To serialize a resource of that jar the converter asks both for a distance and keeps the nearest. The one on disk cannot measure a resource of a jar: `FileResourceImpl.distance` warns and answers 1000, so the container of the jar was chosen, rightly. Only the warning was wrong (and the symmetrical `JarResourceImpl` / `InJarResourceImpl` one).

**Fixed (2026-10-13).** The converter no longer asks a container of the file system for the distance to a resource of a jar, nor the other way round: that distance is `Integer.MAX_VALUE`. `TestRelativePathResourceConverter.testContainersOfDifferentKinds` fails without the fix. Confirmed on a formod-app start with a Formose project opened and a domain diagram displayed (2026-10-13): 62 -> 0 `Could not compute distance` (the same project load, 32 `TestsFlexo` lines in the log, gave 62 before the fix). The start measured just before did not open any project, hence no conversion at all.

### PAMELA-D-3 — The synthetic `<Impl>_<Iface>_Proxy` class replaces the declared implementing class  ·  `DONE`

**Symptom.** When an entity's implementing class is not in the package of its interface, `getImplementingClassForInterface`, `ProxyMethodHandler.getSuperClass()` and `getOverridingSuperClass()` answer a generated class `<ifacePkg>.<Impl>_<Iface>_Proxy` instead of the declared one, as soon as one instance was created.

**Verified.** The Java 11 work-around of `PAMELAProxyFactory.newInstance` (PAMELA-30, 2020) inserts that empty subclass, defined in the interface's package, as the super class of the javassist proxy, through `setProxySuperClass`, which overwrites the super class the factory reports. `ImplementingClassTest.testImplementingClassInAnotherPackage` (interface `implementingclass.Item`, implementation `implementingclass.impl.ItemImpl` set by `setImplementingClassForInterface`) fails: expected `ItemImpl`, got `ItemImpl_Item_Proxy`.

**Consequences.** `XMLSerializer` writes `getOverridingSuperClass().getName()` as the `p:class` attribute, so an entity serialized with its model entity name (an entity not known to the factory's context) and an overridden implementation would carry the synthetic name, which does not exist in a fresh JVM. Not reproduced through serialization. Disappears with the Byte Buddy backend (PAMELA-F-1), which needs no intermediate class.

**Fixed (2026-10-16).** `PAMELAProxyFactory` no longer extends javassist's `ProxyFactory`: it keeps the declared implementing class itself and gives the intermediate class only to the javassist factory it holds, once per factory instead of at each instantiation. The generation of the intermediate class is now synchronized. `ImplementingClassTest` passes; pamela-core 75/75.

Since 2026-10-18 the intermediate class itself is gone with javassist (PAMELA-F-1): the proxy extends the declared implementing class directly.

### PAMELA-D-4 — With javassist, an implementation class loaded by a child class loader is replaced by another copy  ·  `DONE`

**Symptom.** An entity whose implementation class is loaded by a child class loader (child-first, as the jar class loader of a technology adapter), in another package than its interface, gets a proxy that does not extend that implementation class but another copy of it, loaded by the loader of the interface.

**Verified.** `ProxyBackendTest.testImplementationInAChildClassLoader[javassist]` (2026-10-18): `ChildItemImpl`, defined by a `ChildFirstLoader`, implementing `Item` of the parent loader. The intermediate class of the PAMELA-30 work-around is built from the name of the implementation class, resolved by `ClassPool.getDefault()` on the system class path, and defined next to the interface: its super class is whatever class of that name the interface's loader finds. When none is found, `NotFoundException` is printed and the intermediate class extends `Object`. Not observed in an application.

**Fix.** None for the javassist backend, which is to be removed (PAMELA-F-1): the Byte Buddy backend defines the proxy next to the implementation class when its loader sees the interface, and passes the same test.

**Closed (2026-10-18).** javassist and its work-around were removed from PAMELA 2.0 (PAMELA-F-1); `ProxyBackendTest.testImplementationInAChildClassLoader` passes. The defect comes back if the removal commit is reverted.

### PAMELA-D-5 — A model entity that fails to import breaks every later import  ·  `DONE`

**Symptom.** Once a `ModelDefinitionException` was raised while importing an entity (an inconsistent property, a primitive without
default value), every later `PamelaMetaModelLibrary.retrieveMetaModel` of unrelated entities failed with that same exception, in the
same JVM.

**Cause.** `ModelEntityLibrary.importEntity` registers the new entities, then merges their properties; an exception left them in the
library and in `newEntities`, never cleared, so the next import merged the broken entity again.

**Fixed (2026-11-10).** On failure the entities of that import are removed from the library, and `newEntities` is always cleared.
Shown by `TestPersistenceRule.testTransientCannotCarryAnXMLMapping` (an invalid entity on purpose): before the fix, 9 other tests of
pamela-core failed after it.
