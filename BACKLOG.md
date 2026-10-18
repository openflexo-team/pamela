# pamela — Backlog

Features and evolutions to develop in pamela, near to long term. Known bugs go to `KNOWN_DEFECTS.md`
(identifiers `PAMELA-D-<n>`), not here.

Identifiers are stable and never reused: `PAMELA-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### PAMELA-F-1 — Replace javassist by Byte Buddy (pamela 2.0)  ·  `DONE`

**Problem.** Every PAMELA instance is a javassist proxy (`PAMELAProxyFactory extends ProxyFactory`,
`DelegateImplementation extends ProxyFactory`, `ProxyMethodHandler implements MethodHandler`). javassist is
maintained at a slow pace and the ecosystem left it (Hibernate 6 and Mockito moved to Byte Buddy). On Java 11+
it defines proxies with `MethodHandles.privateLookupIn(neighbor).defineClass`, the neighbor being in the
package of the proxy's super class; when that super class is `Object` (no implementation class) javassist
renames the proxy into `javassist.util.proxy.*` and takes `this.getClass()` — `PAMELAProxyFactory`, outside that
package — as neighbor, which fails. The PAMELA-30 work-around generates, with `ClassPool`, an empty subclass in
the interface's package. Bypassing it makes 29 of the 74 pamela-core tests fail, all on that `Object` case
(checked 2026-10-16). The work-around is not thread-safe, swallows its errors, may collide on simple names,
keeps every class loader alive and leaks its synthetic class (PAMELA-D-3). The time bombs on the JDK side are
in javassist itself (`AccessController`/`SecurityManager`, deprecated for removal; reflective
`ClassLoader.defineClass` and `sun.misc.Unsafe` fallbacks).

**Decisions (2026-10-16).**
- Byte Buddy (`net.bytebuddy:byte-buddy` 1.18.x, Apache 2.0, available on openflexo-deps) replaces javassist.
- pamela becomes 2.0: removing `javassist.util.proxy.ProxyObject` from the API breaks diana and openflexo-server
  binary compatibility.
- Short coexistence of both backends, chosen by a system property `pamela.proxy.backend`, to run the same suites
  (pamela, openflexo-core, diana, formod) and formod-app in both modes; then javassist is removed.
- Proxy classes are named `<Iface>$$Pamela$<n>`.

**Steps.**
1. *Decoupling, javassist kept.* `PamelaProxies.isProxy(Object)` / `handlerOf(Object)` replace every
   `instanceof ProxyObject` / `getHandler()` cast (pamela-core, diana `DrawingImpl`, openflexo-server
   `JsonSerializer`, tests); `PamelaMethodHandler` (`invoke(self, method, proceed, args)`) is implemented by
   `ProxyMethodHandler` and `DelegateImplementation`; javassist becomes an `implementation` dependency, so that
   any remaining leak fails to compile; PAMELA-D-3 fixed.
2. *Byte Buddy backend* behind an internal `ProxyBackend`: `subclass(superClass).implement(iface, PamelaProxy)`,
   a handler field, `MethodDelegation` to one dispatcher taking `@This @Origin @SuperMethod(nullIfImpossible = true)
   @AllArguments` (same contract as `MethodHandler`; a null handler during construction calls the super method,
   as javassist's default handler does); `TypeCache` keyed by (super class, interface, set of filtered
   signatures); loaded with `ClassLoadingStrategy.UsingLookup` on a neighbor chosen explicitly: the super class
   if its loader sees the interface, the interface otherwise. No intermediate class.
3. *Switch and removal* once both modes are equivalent.

**Progress.** Step 1 done (2026-10-16), not committed: `PamelaProxies`, `PamelaMethodHandler` and the
package-private `JavassistProxyBackend` (the only holder of the javassist adapter); `PAMELAProxyFactory` and
`DelegateImplementation` hold a javassist `ProxyFactory` instead of extending it; javassist is an
`implementation` dependency; diana `DrawingImpl`, openflexo-server `JsonSerializer` and the pamela-editor test
use `PamelaProxies`. The whole openflexo-dev composite compiles; pamela-core 75/75, pamela-security-patterns
44/44, flexo-foundation-test 540/545 (5 skipped), fml-cli-test 99/99, pamela-editor-core 179/179, http-server
138/138. Committed on branch `2.0` (d2eebb6), version `2.0` (bfb5eaf). Jenkins job `pamela-2.0` (view `3.0`,
upstream `openflexo-utils-1.7`, no downstream yet) built #1 green on 2026-10-16: 75/75 and 44/44, `pamela-core`
and `pamela-security-patterns` `2.0-SNAPSHOT` published, javassist in `runtime` scope in the POM.

**Switch of the 3.0 line (2026-10-16).** `buildconfig` 3.0 sets `pamelaVersion = "2.0"` (openflexo-buildplugin
cb20204); diana (d5d9a8f4) and openflexo-server (b9619e6) use `PamelaProxies`. Jenkins: `gina-2.4` and
`openflexo-core-3.0` are triggered by `pamela-2.0` instead of `pamela-1.6.1` (configurations backed up first).
The cascade started from `openflexo-buildplugin-3.0` ran 111 builds in 37 min: 99 SUCCESS, 12 UNSTABLE in four
jobs already unstable before with the same failures (docx, owl, xslx, ui-3.0-ui-test). The published POMs of
gina and openflexo-core now depend on `pamela-core:2.0-SNAPSHOT`. `openflexo-server-3.0` has no upstream
trigger and was not rebuilt. pamela-editor still uses `buildconfig` 2.99 (pamela 1.6): its test change is not
committed.

**Prototype (2026-10-18), not committed.** `pamela-core/src/test/java/org/openflexo/pamela/prototype/bytebuddy/`
(`PrototypeProxyGenerator`, `ByteBuddyPrototypeTest`, 12 tests green; Byte Buddy 1.18.15 as a test dependency),
each point compared with javassist on the same handler:
- *Packages and class loaders.* `ClassLoadingStrategy.UsingLookup` on the neighbor (super class if its loader
  sees the interface, the interface otherwise) works in the five cases: implementation in the interface's
  package, no implementation (`Object`), implementation in another package, interface in a child-first loader
  with the implementation in the parent, implementation in the child with the interface in the parent. No
  intermediate class: the proxy's super class is the declared implementation class.
- *`proceed`.* `@SuperMethod(nullIfImpossible = true)` gives a `public final` accessor (`describe$accessor$…`),
  invocable from another package without `setAccessible`; `null` for an abstract method, as with javassist.
- *`@Origin`.* Same `Method` as javassist in both cases tested: the interface's declaration (with its
  `@Initializer`) when only the interface declares it; the implementation class's declaration (without the
  annotation) when that class re-declares the method abstract — a pre-existing limit, identical in both.
- *Semantics.* A handled method called by the constructor runs the super method (handler not installed yet);
  checked exceptions go through; `TypeCache` gives one class per (super class, interface, handled signatures),
  also under 8 concurrent threads.
- *Rough cost* (one run, JIT noise): 10,000,000 calls of a concrete intercepted method about 55 ms with
  Byte Buddy vs 75-95 ms with javassist; 100,000 instantiations of an already generated class 4-5 ms vs 28-49 ms.

**Step 2 (2026-10-18), not committed.** `ProxyBackend` (package-private, `pamela.proxy.backend` read once per JVM,
`javassist` by default, the backend logged at first use) with two implementations: `JavassistProxyBackend` (the
former code, PAMELA-30 work-around included) and `ByteBuddyProxyBackend` (`PamelaProxy` interface, a handler field,
one `PamelaProxyDispatcher` per intercepted method, `TypeCache`, `UsingLookup` on the neighbor). `PAMELAProxyFactory`
and `DelegateImplementation` only see `ProxyBackend`; javassist is referenced by `JavassistProxyBackend` alone.
Byte Buddy (1.18.15) is an `implementation` dependency next to javassist.
- *What the handler receives.* `@Origin` was not enough: PAMELA relies on the exact declaration javassist gives
  (`ModelEntity.getInitializers(Method)` looks it up; `PamelaCoreTests1` failed on `init` redeclared in
  `FlexoProcess`). javassist chooses in two steps, reproduced by `ByteBuddyProxyBackend.handledMethods`: which
  methods (interfaces first, then the super class hierarchy, last declaration wins, filter applied to it), and which
  declaration the handler gets (`RuntimeSupport.findSuperClassMethod`: super class hierarchy from its root, each class
  before its interfaces, then the proxy's interfaces). A generic method is matched by its erased signature
  (`TestContainer<T>.setEmbedded(T)`, `ContainmentTest`).
- *Differences kept.* `finalize()` is never intercepted (javassist intercepts it without filter, for
  `DelegateImplementation`); a proxy of a generic interface also declares the resolved signatures as bridges
  (`setEmbedded(TestEmbeddedA)`), which give the handler the erased declaration.
- *Tests.* `ProxyBackendTest`, run on both backends (10 cases each): packages and class loaders (javassist fails
  the implementation in a child loader, PAMELA-D-4), proceed, constructor, checked exceptions, concurrent
  generation, and the parity of the declarations given to the handler, method by method, on six entities.
  pamela-core 95/95 and pamela-security-patterns 44/44 in both modes (`JAVA_TOOL_OPTIONS=-Dpamela.proxy.backend=…`).
- *Downstream, openflexo-dev composite, both modes (2026-10-18).* Identical results: gina-core 25, gina-swing 5,
  diana-core-test 14, diana-swing 21, flexo-foundation-test 540 (+5 skipped), fml-cli-test 99, pamela-editor-core
  179, http-server 138, integration-tests 37, multi-process-challenge 13, xlsx-ta-test 34 (+4), jdbc-ta-test 34
  (+31), all green. formod not run: `:formod:b-ta-parser:processResources` uses the SableCC output without a task
  dependency, a Gradle configuration error unrelated to PAMELA. Still to do before step 3: formod-app run by hand
  with `-Dpamela.proxy.backend=bytebuddy` (2026-10-18, on a JDK 17: tested interactively by the user, no difference seen).
- *Jenkins (2026-10-18).* baae664 pushed; `pamela-2.0` #3 and its cascade, 107 builds in 24 min (default backend
  still javassist, Byte Buddy now in the POM): 94 SUCCESS, 12 UNSTABLE in the four jobs already unstable (docx 5
  failures, owl 1, xslx 3, ui-3.0-ui-test 6, against 4-5, 1, 3, 9 before), 1 FAILURE (`openflexo-csv-3.0` #10: "Gradle
  build daemon disappeared unexpectedly", no test run; #11 of the same cascade green, 117 + 18).
**Step 3, first part (2026-10-18), not committed.** `bytebuddy` is the default backend (`ProxyBackend.Selection`);
`-Dpamela.proxy.backend=javassist` brings javassist back. Without the property: pamela-core 95/95, security-patterns
44/44; in the composite, the same 12 suites as above with the same counts, plus formod b-ta-test 146/146 and
formod-test 32/32 (runnable again since its b-ta-parser build fix); with javassist forced, pamela unchanged.
Pushed as 02e23db; `pamela-2.0` #4 and its cascade (2026-10-18): 108 builds in 29 min, 96 SUCCESS, 12 UNSTABLE in the
same four jobs with the same failures as the previous cascade (docx 5, owl 1, xslx 3, ui-3.0-ui-test 6), no
FAILURE. The test logs of `pamela-2.0` and `openflexo-csv-3.0` show `PAMELA proxy backend: bytebuddy`.
`openflexo-server-3.0` has no upstream trigger and was not rebuilt.

- *Performance (2026-10-18).* `pamela-perf-tests` is not a benchmark but a generator of plain/PAMELA model sources
  (to compile and run separately); the benchmark is `pamela-core` `PerformanceTest.main()` (not a JUnit test), run
  three times per backend on the test class path. Building 97,656 objects 9 times: 1250-1300 ms with javassist vs
  908-1000 ms with Byte Buddy (`ArrayList` lists), 1416-1594 vs 1151-1283 ms (`Vector`); heap growth 1023-1050 vs
  711-722 MB. Dumb and manipulation scenarios equal (120-155 ms). The javassist figures include the
  `HandlerAdapter` added per instance in step 1. Note: the "regular classes" column of that benchmark is PAMELA too
  (`testModel` runs both with a `PamelaModelFactory`), so it compares two list implementations, not PAMELA with plain
  Java.

**Step 3, second part: javassist removed (2026-10-18).** `JavassistProxyBackend`, the PAMELA-30 work-around
(`ClassPool`, intermediate `<Impl>_<Iface>_Proxy` classes) and the javassist dependency are gone; `ByteBuddyProxyBackend`
is the only backend. `pamela.proxy.backend` is ignored, with a warning when it names anything but `bytebuddy`.
`ProxyBackendTest` runs on Byte Buddy alone; the parity with javassist, checked method by method before the removal,
is kept as fixed expectations (`testDeclarationsGivenToTheHandler`). pamela-core 85/85, pamela-security-patterns 44/44;
no javassist left in the runtime class path of pamela-core. javassist remains in the workspace only for Hibernate
5.2 (openflexo-jdbc), which generates its own proxies with it: without pamela imposing 3.30.2, Hibernate fell back
to the 3.20 it declares, which fails on Java 17 (`InaccessibleObjectException` on `ClassLoader.defineClass`,
`BasicTest.testCreateData`), so `jdbc-ta` now declares javassist 3.30.2-GA itself; it is also an ignored package
name in connie's and gina's `LoadedClassesInfo`. In the openflexo-dev composite, without javassist in pamela: the
14 downstream suites give the same counts as before (jdbc-ta-test 34 + 31 skipped once `jdbc-ta` pins javassist).

**Going back if Byte Buddy causes a problem.** The removal of the code is one commit on the `2.0` branch, titled
"Remove javassist, Byte Buddy only backend (PAMELA-F-1)", committed apart from this documentation so that
it can be reverted alone. Reverting it (`git revert <that commit>`) restores
`JavassistProxyBackend`, the javassist dependency and the choice by `-Dpamela.proxy.backend=javassist`, with Byte
Buddy still the default: a JVM, a test run or an application can then be switched back without rebuilding the
consumers (the API, `PamelaProxies`, is the same in both). Then build `pamela-2.0` on Jenkins, which republishes
`pamela-core:2.0-SNAPSHOT` and starts the 3.0 cascade. To compare both backends on a case, run it twice with
`JAVA_TOOL_OPTIONS=-Dpamela.proxy.backend=javassist` and without (test JVMs, Gradle `run` tasks and the composite
pick it up).

**Acceptance.** pamela-core and pamela-security-patterns green in both modes; openflexo-core, diana and formod
test suites unchanged through the openflexo-dev composite; formod-app runs; instantiation and invocation
timings (`pamela-perf-tests`) not worse; no `javassist` reference left in the workspace except Hibernate's own.

### PAMELA-F-2 — Merge the 2022-2023 add-on / DPF work of branch `2.0-addon-2023`  ·  `DEFERRED`

**Where.** Branch **`2.0-addon-2023`** on `origin` (tip `acc9d94`, 2023-06-26, "Implemented XOrAssociation"; forked
from `1.6.1` at `9a27fd2`, 2023-04-24). It was the former `2.0` branch, renamed on 2026-10-16 when `2.0` was
restarted from `1.6.1` for PAMELA-F-1. 38 commits (34 without merges), 171 files, about +10,000 / -1,900 lines.

**Content.**
- PamelaAddOn mechanism in pamela-core (`org.openflexo.pamela.addon`: `EntityAddOn`, `EntityAddOnInstance`),
  `PamelaModel`, `PatternInstanceEvent`, pre/post-condition checking of pattern instances, `MonitoringStrategy`
  set programmatically, improved `InvocationTargetException` / `ModelExecutionException` handling.
- JML moved out of pamela-core into its own module `pamela-jml`.
- `pamela-ppf`: DPF-style predicates as annotations (`@Injective`, `@NonOverlapping`, irreflexive, total, XOr
  association, m..n multiplicities); tests in `pamela-dpf-test`.
- Security patterns reworked (`pamela-security-patterns-test`, multiple authentication information, Spring
  security use case fixed).

**State (checked 2026-10-16).** Does not compile against today's dependencies: `PamelaModelFactory` still
implements `IObjectGraphFactory`, abandoned on `1.6.1` in `aa01ed9` (2024-12). Tests not run. A trial merge into
`2.0` gives 3 textual conflicts (`.gitignore`, `PamelaModelFactory`, `ProxyMethodHandler`), but the real risk is
semantic: `ProxyMethodHandler` was split into specialized handlers on `1.6.1` (2026-04) and is the class
PAMELA-F-1 rewires.

**When.** After PAMELA-F-1 (the proxy layer must be settled first). Moving JML to its own module changes the API
(package `org.openflexo.pamela.jml`): check openflexo consumers.
