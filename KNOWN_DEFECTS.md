# pamela — Known defects

Bugs of the modeling framework (pamela-core) that are known and not fixed yet. Each entry says what is verified, how to reproduce it, and how to work around it. Features and evolutions go to `BACKLOG.md`, not here.

Identifiers are stable and never reused: `PAMELA-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### PAMELA-D-1 — "already recording" is logged by the undo manager when an action runs inside another  ·  `TODO`

**Symptom.** `[PLEASE TRACK ME] : UndoManager exception: already recording <Unidentified recording> | <name of an action>` (`UndoManager.startRecording`), 3 times when a Formose project is opened and many more while it is used (creating an element, instantiating a methodology, dropping a shape), always with the name of the action that was recording (`Create element`, `Gives formose nature`, `CreateFunctionalGoal`…).

**Read.** The message is the authors' own marker (`[PLEASE TRACK ME]`) in `UndoManager.startRecording`: a second edition starts while one is recording, as when an action runs an embedded action. Whether the second edition is lost or merged, and whether undo then undoes the right thing, was not examined.
