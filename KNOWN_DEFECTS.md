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
