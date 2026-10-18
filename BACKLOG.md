# pamela — Backlog

Features and evolutions to develop in pamela, near to long term. Known bugs go to `KNOWN_DEFECTS.md`
(identifiers `PAMELA-D-<n>`), not here.

Identifiers are stable and never reused: `PAMELA-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### PAMELA-F-1 — Replace javassist by Byte Buddy (pamela 2.0)  ·  `IN PROGRESS`

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

**To prove with a prototype before step 2.** `UsingLookup` with a neighbor in a child class loader;
`@SuperMethod` invocable without `setAccessible` on the caller side; the `Method` given by `@Origin` carries the
interface's annotations (`ProxyMethodHandler` reads `@Initializer` from it).

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
