# Gamelan DAG Module (Optional Upgrade)

This doc describes how to add a **DAG specialization module** on top of the existing Gamelan core without breaking the foundation.

---

## ✅ Current Baseline (From Code)

Gamelan is **flow‑first by default** and supports cycles for agentic workflows.

### Evidence in current code

* **Flow default**  
  `workflow-gamelan/core/gamelan-engine-spi/src/main/java/tech/kayys/gamelan/engine/workflow/WorkflowDefinition.java`
  - `mode` defaults to `FLOW`
* **Cycle checks only in DAG mode**  
  `workflow-gamelan/core/gamelan-engine/src/main/java/tech/kayys/gamelan/workflow/WorkflowValidator.java`
  - `hasCycles()` runs only when `mode == DAG`
* **Dependency‑based execution** using `dependsOn` in `WorkflowDefinition`
* **Orchestrator + scheduler** are compatible with topological ordering when DAG mode is enabled

### Implication

By default, **Gamelan runs as a flow engine** (cycles allowed).  
`mode: DAG` is an **explicit opt‑in**.

---

## Why Add a DAG Module Anyway

Even though DAG is already supported, a dedicated module gives:

1. **Stricter DAG validation**
   - detect orphans
   - enforce single root
   - topological ordering guarantee
2. **Optimized scheduling**
   - more parallelism
   - batched readiness
   - deterministic order
3. **DAG‑specific features**
   - dataset lineage
   - per‑node retries
   - cron/batch policies

---

## ✅ Non‑Breaking Upgrade Strategy

### Keep current default behavior
* **No change** to existing workflows
* Existing validator remains default

### Add DAG module as optional profile
* `gamelan-dag` module plugged into engine
* Activated only when `mode: DAG`

---

## Proposed Module Structure

```
workflow-gamelan/
  core/
    gamelan-engine/        # core engine (unchanged)
  extensions/
    gamelan-dag/           # optional DAG specialization
      DagValidator
      DagScheduler
      DagExecutionPolicy
```

---

## Workflow Mode (Safe Extension)

Add a `mode` field to workflow definitions:

```yaml
id: pipeline_example
mode: DAG   # DAG | FLOW | STATE
```

### Backward compatibility
* If `mode` is missing, default to **FLOW** (agentic/loop‑friendly)
* `DAG` explicitly enforces acyclic validation and topological scheduling

---

## Suggested Components

### 1) DagValidator
* Extends existing validation
* Adds orphan detection + single root

### 2) DagScheduler
* Executes only when all parents completed
* Uses topological readiness queue

### 3) DagExecutionPolicy
* Per‑node retries (no graph rewind)
* Batch‑friendly execution

---

## DAG Workflow Example

```yaml
id: nightly_pipeline
mode: DAG
nodes:
  - id: extract
    type: http
  - id: transform
    type: tool
    dependsOn: [extract]
  - id: load
    type: storage
    dependsOn: [transform]
```

Recommended runtime flags:
* `gamelan.dag.plugin.enabled=true`
* `gamelan.dag.scheduler.enabled=true` (topological ordering)

---

## Validation Rules (Default)

| Rule | Description |
| --- | --- |
| No cycles | Cycles are rejected in `mode: DAG` |
| Single root | Enforced unless `allowMultipleRoots=true` |
| No orphan nodes | Enforced unless `allowOrphanNodes=true` |
| Reachable nodes | All nodes must be reachable from a root |
| Depth limit | Max DAG depth is enforced |
| Width limit | Max DAG width is enforced |

---

## ⚠️ Foundation Safety Notes

Do **not** replace the existing engine:
* Keep `WorkflowOrchestrator` intact
* Add DAG module as a **plug‑in**

This preserves:
* current execution contracts
* existing clients + SDKs
* production stability

---

## Recommended Doc Sentence

> “Gamelan is a flow‑based workflow engine.  
> A DAG workflow is treated as a constrained flow where cycles are disallowed and execution is scheduled using topological ordering.”

---

## Feature Flags

* `gamelan.dag.plugin.enabled=true`  
  Enables DAG validator plugin when `mode: DAG`
* `gamelan.dag.scheduler.enabled=true`  
  Enables optional DAG topological ordering of ready nodes

---

## Next Step

If you want, I can:
1. Add a `mode` field to workflow schema docs
2. Draft a `DagValidator` implementation under a new module
3. Wire DAG scheduling into `WorkflowOrchestrator` behind a feature flag
