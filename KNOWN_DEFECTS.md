# pamela — Known defects

Bugs of the modeling framework (pamela-core) that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work around it. Features and evolutions go to `BACKLOG.md`, not here.

Identifiers are stable and never reused: `PAMELA-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### PAMELA-D-1 — "already recording" is logged by the undo manager when an action runs inside another  ·  `TODO`

**Symptom.** `[PLEASE TRACK ME] : UndoManager exception: already recording <Unidentified recording> | <name of an action>` (`UndoManager.startRecording`), 3 times when a Formose project is opened and many more while it is used (creating an element, instantiating a methodology, dropping a shape), always with the name of the action that was recording (`Create element`, `Gives formose nature`, `CreateFunctionalGoal`…).

**Read.** The message is the authors' own marker (`[PLEASE TRACK ME]`) in `UndoManager.startRecording`: a second edition starts while one is recording, as when an action runs an embedded action. Whether the second edition is lost or merged, and whether undo then undoes the right thing, was not examined.

### PAMELA-D-2 — "Could not compute distance from a File for a non-file resource" is logged for a resource the converter addresses correctly  ·  `DONE`

**Symptom.** 62 warnings at the start of formod-app, on the AWT thread: `Could not compute distance from a File for a non-file resource: …formod-rc-2.99-SNAPSHOT.jar!/DomainModel/Icons/Variable.png` (38) and `Enumeration.png` (24). Every one comes from `RelativePathResourceConverter.computeRelativePath`, the only caller of `Resource.distance`.

**Verified.** A diagram has two container resources: its own directory, on disk (set by `DiagramFactory`), and the one of its diagram specification, in a jar (`DiagramResourceImpl.setMetaModelResource`). To serialize a resource of that jar the converter asks both for a distance and keeps the nearest. The one on disk cannot measure a resource of a jar: `FileResourceImpl.distance` warns and answers 1000, so the container of the jar was chosen, rightly. Only the warning was wrong (and the symmetrical `JarResourceImpl` / `InJarResourceImpl` one).

**Fixed (2026-10-13).** The converter no longer asks a container of the file system for the distance to a resource of a jar, nor the other way round: that distance is `Integer.MAX_VALUE`. `TestRelativePathResourceConverter.testContainersOfDifferentKinds` fails without the fix. Confirmed on a formod-app start with a Formose project opened and a domain diagram displayed (2026-10-13): 62 -> 0 `Could not compute distance` (the same project load, 32 `TestsFlexo` lines in the log, gave 62 before the fix). The start measured just before did not open any project, hence no conversion at all.

### PAMELA-D-3 — The synthetic `<Impl>_<Iface>_Proxy` class replaces the declared implementing class  ·  `DONE`

**Symptom.** When an entity's implementing class is not in the package of its interface, `getImplementingClassForInterface`, `ProxyMethodHandler.getSuperClass()` and `getOverridingSuperClass()` answer a generated class `<ifacePkg>.<Impl>_<Iface>_Proxy` instead of the declared one, as soon as one instance was created.

**Verified.** The Java 11 work-around of `PAMELAProxyFactory.newInstance` (PAMELA-30, 2020) inserts that empty subclass, defined in the interface's package, as the super class of the javassist proxy, through `setProxySuperClass`, which overwrites the super class the factory reports. `ImplementingClassTest.testImplementingClassInAnotherPackage` (interface `implementingclass.Item`, implementation `implementingclass.impl.ItemImpl` set by `setImplementingClassForInterface`) fails: expected `ItemImpl`, got `ItemImpl_Item_Proxy`.

**Consequences.** `XMLSerializer` writes `getOverridingSuperClass().getName()` as the `p:class` attribute, so an entity serialized with its model entity name (an entity not known to the factory's context) and an overridden implementation would carry the synthetic name, which does not exist in a fresh JVM. Not reproduced through serialization. Disappears with the Byte Buddy backend (PAMELA-F-1), which needs no intermediate class.

**Fixed (2026-10-16).** `PAMELAProxyFactory` no longer extends javassist's `ProxyFactory`: it keeps the declared implementing class itself and gives the intermediate class only to the javassist factory it holds, once per factory instead of at each instantiation. The generation of the intermediate class is now synchronized. `ImplementingClassTest` passes; pamela-core 75/75.

### PAMELA-D-4 — With javassist, an implementation class loaded by a child class loader is replaced by another copy  ·  `DEFERRED`

**Symptom.** An entity whose implementation class is loaded by a child class loader (child-first, as the jar class loader of a technology adapter), in another package than its interface, gets a proxy that does not extend that implementation class but another copy of it, loaded by the loader of the interface.

**Verified.** `ProxyBackendTest.testImplementationInAChildClassLoader[javassist]` (2026-10-18): `ChildItemImpl`, defined by a `ChildFirstLoader`, implementing `Item` of the parent loader. The intermediate class of the PAMELA-30 work-around is built from the name of the implementation class, resolved by `ClassPool.getDefault()` on the system class path, and defined next to the interface: its super class is whatever class of that name the interface's loader finds. When none is found, `NotFoundException` is printed and the intermediate class extends `Object`. Not observed in an application.

**Fix.** None for the javassist backend, which is to be removed (PAMELA-F-1): the Byte Buddy backend defines the proxy next to the implementation class when its loader sees the interface, and passes the same test.
