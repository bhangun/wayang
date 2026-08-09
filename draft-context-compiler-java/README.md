# context-compiler-java

A Java/Quarkus port of Emmimal P Alexander's Context Compiler
(original write-up: "Coding Agents Don't Need Bigger Context Windows, They Need
a Context Compiler", towardsdatascience.com).

Same core idea, adapted for a statically-typed, import-mandatory language and a
Quarkus reactive backend instead of a Python CLI:

```
Target file --> SymbolResolver --> Skeletonizer --> ContextCompiler --> CompiledContext
              (reachability graph)  (body strip)     (tier assembly)
```

## Modules

- **context-compiler-core** -- pure Java, one dependency (JavaParser). No Quarkus,
  no framework coupling. Usable from any JVM app.
- **context-compiler-quarkus** -- CDI wiring + a reactive REST endpoint over the
  core library, meant to be dropped into an existing Quarkus app.

## Architecture

Abstraction (`api` package) and implementation (`impl` package) are separated, the
same pattern used elsewhere in this codebase for repository swap-ability:

| Interface | Default impl | Role |
|---|---|---|
| `SymbolResolver` | `HeuristicSymbolResolver` | import + bare-name reachability (faithful port) |
| | `TypeAwareSymbolResolver` | wraps the heuristic pass with JavaParser's symbol solver to narrow name collisions -- see "What's different from the Python original" below |
| `Skeletonizer` | `JavaParserSkeletonizer` | strips bodies to signatures; also does member-pruning and signature-only digests |
| `TokenEstimator` | `DefaultTokenEstimator` | `chars/4` heuristic, same as the original |
| `RelevanceScorer` | `DefaultRelevanceScorer` | mechanical (hop + call-site + attribution) ranking, no embeddings |
| `ContextPlanner` | `DefaultContextPlanner` | maps task intent to hop depth + token budget |
| `ContextCompiler` | `DefaultContextCompiler` | fixed-hop, binary tier assembly |
| `BudgetedContextCompiler` | `DefaultBudgetedContextCompiler` | ranked, budget-aware, five-tier assembly |

`compile(repoRoot, targetFile, maxHops)` takes the repo root and hop depth per call
rather than binding them at construction time (unlike the Python `ContextCompiler
(root, max_hops)`). That's a deliberate change: Quarkus CDI beans are typically
application-scoped singletons serving concurrent requests against potentially
different repos, so nothing in this port holds per-repo state on `this`.

## Serving small / open-weight models without losing fidelity

`BudgetedContextCompiler` + `ContextPlanner` exist for one specific goal: fit a
tight context window (an 8K local model, for example) without the two usual ways
of getting there -- naive truncation, or AI-generated summarization -- either of
which either drops something needed or quietly misrepresents it.

The lever instead is **scope, not fidelity**. Every tier below `FULL_SOURCE` is
still an exact substring of the original file; what changes is how much of the
file's *scope* survives:

```
FULL_SOURCE         the target file, unchanged
SKELETON            every member's signature + Javadoc, bodies stripped
SKELETON_PRUNED     only the members confidently used along the reachable
                    chain (see ReachabilityResult.usedMembers()); omitted
                    members are disclosed with a count, never hidden silently
SIGNATURE_DIGEST    bare signatures, one line per member, no Javadoc
EXCLUDED            not included at all
```

`DefaultBudgetedContextCompiler` ranks every reachable file with `RelevanceScorer`
(hop distance + call-site count + attribution confidence -- all numbers the
resolver already produced mechanically, nothing semantic or embedding-based) and
greedily assigns each the richest tier that still fits the remaining budget,
falling back tier by tier rather than truncating mid-file. `ContextPlanner` decides
the starting hop depth and budget split from task intent (`BUG_FIX` stays shallow
and precise; `REFACTOR` goes wider since callers need to see the interface holds;
`EXPLORATION` casts the widest net).

## On the larger "context compilation pipeline" idea

A fuller design was considered and deliberately scoped down: `Planner -> Collector
-> Resolver -> Filter -> Reducer -> Ranker -> Budgeter -> Assembler -> Validator`,
generalized across code, tickets, and security data with a plugin per domain. Two
reasons it isn't here:

1. That design's `Reducer` step includes `Summary` and `Embedding` as reduction
   strategies. Both are lossy -- a summary can misstate what the code does, and an
   embedding is unreadable to the model it's meant to serve. That contradicts the
   goal this module is actually built around (see above), so neither strategy is
   implemented here.
2. The rest of that pipeline -- `Collector`/`Resolver`/`Filter`/`Reducer`/`Ranker`/
   `Budgeter`/`Assembler` as separate plugin interfaces across several domain
   plugins -- is premature generalization for a project with exactly one proven
   domain (Java). The right trigger for extracting a domain-independent interface
   is a second real implementation to generalize *from* (e.g. once a Dart/Flutter
   resolver exists for `tenun_pro`-style projects), not before.

The one piece adopted is `ContextPlanner`, because it's genuinely new, doesn't
require the multi-domain generalization to be useful on its own, and answers a
real question the rest of this project didn't: the same target file needs a
different hop depth and budget split depending on what the edit is actually for.

## What's different from the Python original

- **Java's blind spots aren't Python's.** There's no `getattr()`/`importlib` -- the
  static-analysis-invisible equivalents are reflection (`Class.forName`,
  `Method.invoke`) and, specific to a Quarkus reactive backend, annotation-driven
  wiring with no direct call site: `@Observes` (CDI events), `@ConsumeEvent`
  (Vert.x event bus), `@Incoming`/`@Outgoing` (reactive messaging), `@Scheduled`.
  Both are flagged the same way the original flags `getattr()` and `@receiver`.
- **A real (if scoped) fix for the name-collision blind spot.** The Python resolver
  can only ever report `.save()`-style collisions, because Python's dynamic typing
  gives it nothing to check further. Java's static types make partial resolution
  possible: `TypeAwareSymbolResolver` re-checks the heuristic pass's collisions
  against JavaParser's `JavaSymbolSolver` and narrows a collision to the file that
  actually declares the resolved method, wherever it can. It still degrades to the
  same disclosed-collision behavior for anything it can't resolve (external jars,
  runtime-only types) -- it improves precision, it doesn't pretend to be a full
  type checker.
- **Skeletons are syntactically valid Java, not just Python-shaped.** Python's `...`
  is a legal expression on its own. A Java method body isn't legal with nothing in
  it if the method returns a value, so stripped bodies get a type-appropriate
  default return (`return null;`, `return 0;`, `return false;`, ...) instead of a
  placeholder token -- still near-zero cost, but the skeleton stays parseable Java.
- **No per-instance caching.** The original notes `ModuleIndex` isn't cached across
  `compile()` calls and neither is this -- deliberately, again for CDI-singleton
  thread-safety. If you're calling this repeatedly against the same repo (e.g. from
  the REST endpoint on every keystroke of an editor plugin), cache `ModuleIndex` at
  the call site keyed by repo root, invalidated on a file-watch or short TTL. This
  is the most impactful thing to add before using it in a tight loop.

## What's carried over as-is (same trade-offs, same honesty about them)

- Reachability is still name-only for anything an import can't explain -- it trades
  soundness for speed, same as the original, and every blind spot it can't resolve
  is reported on `ReachabilityResult`, not silently dropped.
- Token counts are still `chars/4`. Swap `DefaultTokenEstimator` for a real
  tokenizer (e.g. jtokkit) for exact counts -- worth doing before trusting a tight
  budget against a specific small model's real vocabulary.
- Member attribution in `ReachabilityResult.usedMembers()` is a conservative
  heuristic (see `HeuristicSymbolResolver`'s javadoc), not a type-checked fact --
  it over-includes rather than risks dropping something used.

## Running the demo (core module only, no Quarkus needed)

```bash
cd context-compiler-core
mvn exec:java
```

Runs two passes against a small synthetic repo built in a temp directory (a
service/repository pair with a `save()` collision, a reflection-based dispatcher,
and a `@ConsumeEvent`-annotated handler):

1. the original fixed-hop compile
2. a planner-driven, budget-aware compile under a deliberately tight budget, so
   the printed output actually shows tiers spilling from full skeleton down to
   pruned skeleton down to signature digest as space runs out

## Using it from your own Quarkus app

Add `context-compiler-core` (and `context-compiler-quarkus` if you want the CDI
beans + REST endpoints) as dependencies, then inject either compiler directly:

```java
@Inject
ContextCompiler contextCompiler; // fixed hop depth, binary tiering

@Inject
BudgetedContextCompiler budgetedContextCompiler; // ranked, budget-aware

@Inject
ContextPlanner contextPlanner; // task intent -> hop depth + budget

ContextPlan plan = contextPlanner.plan(TaskIntent.BUG_FIX, 8_000);
CompiledContext compiled = budgetedContextCompiler.compile(
        repoRoot, targetFile, plan.maxHops(), plan.tokenBudget());
String prompt = compiled.toPromptString();
```

or hit the bundled endpoints:

```
POST /api/context-compiler/compile
{ "repoRoot": "/path/to/mediapulse-backend", "targetFile": "/path/to/SomeResource.java", "maxHops": 2 }

POST /api/context-compiler/compile/planned
{ "repoRoot": "/path/to/mediapulse-backend", "targetFile": "/path/to/SomeResource.java", "intent": "BUG_FIX", "contextWindowTokens": 8000 }
```

## A note on this build

This was written and reviewed for correctness (brace/paren balance, import
correctness, JavaParser API shapes) but **not compiled** -- this sandbox has no
access to Maven Central, only a fixed allow-list of registries (npm, PyPI,
crates.io, GitHub), so `mvn compile` can't run here. Run `mvn -q compile` on your
end before wiring it into `mediapulse-backend`; if anything doesn't compile, it's
most likely a JavaParser API surface detail (method name/signature) rather than a
structural issue, since a full manual read-through found the logic sound.

## License

MIT, matching the original.
