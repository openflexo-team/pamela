# pamela — Known defects

Bugs of the modeling framework (pamela-core) that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work around it. Features and evolutions go to `BACKLOG.md`, not here.

Identifiers are stable and never reused: `PAMELA-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

Fixed defects are moved to [`FIXED_DEFECTS.md`](./FIXED_DEFECTS.md) once their status is `DONE`.

---

### PAMELA-D-1 — "already recording" is logged by the undo manager when an action runs inside another  ·  `TODO`

**GitHub:** [openflexo-team/pamela#6](https://github.com/openflexo-team/pamela/issues/6)

**Symptom.** `[PLEASE TRACK ME] : UndoManager exception: already recording <Unidentified recording> | <name of an action>` (`UndoManager.startRecording`), 3 times when a Formose project is opened and many more while it is used (creating an element, instantiating a methodology, dropping a shape), always with the name of the action that was recording (`Create element`, `Gives formose nature`, `CreateFunctionalGoal`…).

**Read.** The message is the authors' own marker (`[PLEASE TRACK ME]`) in `UndoManager.startRecording`: a second edition starts while one is recording, as when an action runs an embedded action. Whether the second edition is lost or merged, and whether undo then undoes the right thing, was not examined.

### PAMELA-D-7 — TypeConverter does not read back the type void, so a concept with a void behaviour cannot be cloned or copied  ·  `TODO`

**GitHub:** [openflexo-team/pamela#16](https://github.com/openflexo-team/pamela/issues/16)

**Symptom (reproduced 2026-11-19, branch 2.0).** Copying a FlexoConcept in the FML editor of Openflexo 3.0 does nothing. The console
shows `NullPointerException: … CopyAction.getClipboard() is null`, and `Openflexo.err.log` holds the real failure:

```
ModelExecutionException … InvalidDataException message: Supplied value represents a type not found: void
	at org.openflexo.pamela.factory.CloningHandler.finalizeClone(CloningHandler.java:296)
Caused by: org.openflexo.pamela.exceptions.InvalidDataException: Supplied value represents a type not found: void
	at org.openflexo.pamela.converter.TypeConverter.convertFromString(TypeConverter.java:181)
	at org.openflexo.pamela.factory.StringEncoder.fromString(StringEncoder.java:96)
	at org.openflexo.pamela.factory.CloningHandler.finalizeClone(CloningHandler.java:293)
```

Any concept that declares a behaviour returning `void` fails to clone, so copy and paste fails for most concepts.

**Cause.** `TypeConverter.convertFromString` maps the primitive names `boolean`, `byte`, `int`, `long`, `short`, `float`, `double`
and `char` to their `TYPE`, but not `void`. It then calls `Class.forName("void")`, which throws. Cloning re-reads a string-convertible
`Type` property from its text, so a return type of `void` (`Void.TYPE`) cannot be cloned.

**How to reproduce.** In openflexo-core, `KnownDefectsCloneVoidBehaviourTest` (fml-parser), run with `OPENFLEXO_KNOWN_DEFECTS=1`. It
clones a concept declaring `public void doIt() { }`. At the PAMELA level: `new TypeConverter(Map.of()).convertFromString("void", null)`
throws instead of returning `Void.TYPE`.

**Where to start.** `pamela-core/src/main/java/org/openflexo/pamela/converter/TypeConverter.java`, `convertFromString`: add the case
`"void"` → `Void.TYPE`. Check that `convertToString(Void.TYPE)` gives `void`, and add a unit test of the round trip for every primitive
type.

**Acceptance.** The clone of a concept with a `void` behaviour succeeds and keeps the return type. The known-defect test passes.

**Workaround.** None for copy: create the concept again by hand.

### PAMELA-D-8 — Undoing a deletion leaves the restored objects deleted and their lists in reverse order  ·  `TODO`

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

**How to reproduce.** In openflexo-core, `KnownDefectsUndoDeleteConceptTest` (fml-parser), run with `OPENFLEXO_KNOWN_DEFECTS=1`. It
parses a VirtualModel whose concept `Root1` contains `Alpha` (which contains `Leaf`) and `Beta`, records `root1.delete()` in the undo
manager of the editing context, undoes it, and checks the state.

**Hypotheses (not checked).**
- The `deleted` flag of `ObjectLifecycleHandler` is not reset by `undelete`.
- `DefaultMultiplePropertyImplementation.undelete` re-adds the old values one by one, and the inverse properties re-add the
  objects in an order that ends up reversed.

**Workaround.** Do not undo a deletion: reload the unit from its last save.
