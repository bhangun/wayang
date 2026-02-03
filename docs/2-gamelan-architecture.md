

# GAMELAN WORKFLOW ENGINE


## Gamelan Internal Modules

- ✔ Orchestrator
- ✔ Executor Selection & Dispatch
- ✔ Node Executors
- ✔ State & History
- ✔ Error handling
- ✔ Callback / Eventing
- ✔ Plugin-based extensibility

---

## 🎼 Gamelan Workflow Engine — Internal Modules (C4 Component)

```mermaid
C4Component
title Gamelan Workflow Engine - Internal Modules

Container(gamelan, "Gamelan Workflow Engine", "Quarkus / Java", "Executes workflows and coordinates nodes")

Component(orchestrator, "Workflow Orchestrator", "Core Engine", "Controls execution lifecycle of workflows")

Component(defParser, "Workflow Definition Parser", "YAML/JSON", "Parses and validates workflow definitions")

Component(tokenMgr, "Execution Token Manager", "Runtime", "Tracks tokens, steps, and transitions")

Component(nodeRegistry, "Node Registry", "Plugin SPI", "Registers available node types")

Component(nodeExecutor, "Node Executor", "SPI", "Executes individual workflow nodes")

Component(executorSelector, "Executor Selection", "Dispatch Engine", "Chooses target executor from registry")

Component(dispatchPolicy, "Dispatch Policies", "RoundRobin, LeastLoad, Weighted", "Pluggable dispatch strategy")

Component(stateStore, "State Store", "Redis/DB", "Persists workflow runtime state")

Component(historyStore, "Execution History Store", "DB", "Stores audit trail and logs")

Component(errorHandler, "Error Handler", "Policy Engine", "Retry, compensation, and fallback logic")

Component(callbackMgr, "Callback Manager", "Event System", "Handles async callbacks & webhooks")

Component(eventBus, "Internal Event Bus", "In-Memory / Kafka", "Publishes workflow lifecycle events")

Component(metricCollector, "Metrics & Tracing", "OpenTelemetry", "Collects observability data")

Rel(orchestrator, defParser, "Loads")
Rel(orchestrator, tokenMgr, "Manages")
Rel(orchestrator, nodeRegistry, "Looks up nodes")
Rel(orchestrator, nodeExecutor, "Invokes")
Rel(orchestrator, executorSelector, "Selects executor")

Rel(executorSelector, dispatchPolicy, "Uses")

Rel(orchestrator, stateStore, "Persists state")
Rel(orchestrator, historyStore, "Writes history")

Rel(orchestrator, errorHandler, "Delegates failures to")

Rel(orchestrator, callbackMgr, "Registers callbacks")
Rel(callbackMgr, eventBus, "Publishes events")

Rel(orchestrator, metricCollector, "Emits metrics")
Rel(nodeExecutor, metricCollector, "Emits metrics")

Rel(errorHandler, eventBus, "Emits failure events")
Rel(orchestrator, eventBus, "Emits lifecycle events")
```

---

## 🧩 Module Responsibilities (short & sharp)

**Workflow Orchestrator**
→ brain of the engine

**Definition Parser**
→ validates `.wy` / JSON / YAML

**Execution Token Manager**
→ manages flow control (fork/join/loop)

**Node Registry (SPI)**
→ plugin system for:

* Agent nodes
* EIP nodes
* BPMN nodes
* NLP nodes
* Time-series nodes

**Executor Selection + Dispatch Policies**
→ decides:

* local executor
* remote executor
* executor instance

**State Store**
→ runtime snapshot

**History Store**
→ audit trail

**Error Handler**
→ retry, backoff, circuit breaker, compensation

**Callback Manager**
→ async node completion (webhook, message queue)

**Event Bus**
→ decoupled signaling

**Metrics Collector**
→ observability

---



## Gamelan Flowchart
Nice, let’s move on to the **next one: Gamelan internal flowchart** 🎼
This shows **how a workflow is executed inside Gamelan**, step by step (runtime behavior, not structure).

---

## 🔁 Gamelan Workflow Engine — Internal Flowchart

```mermaid
flowchart TD
    A[Receive Workflow Trigger] --> B[Load Workflow Definition]

    B --> C[Create Execution Token]
    C --> D[Persist Initial State]

    D --> E{Next Node Available?}

    E -- No --> Z[Workflow Completed]

    E -- Yes --> F[Lookup Node in Node Registry]

    F --> G[Select Node Executor]

    G --> H[Executor Selection]

    H --> I{Dispatch Policy}
    I -- RoundRobin --> I1[Pick Next Target]
    I -- LeastLoad --> I2[Pick Least Busy Target]
    I -- Weighted --> I3[Pick Weighted Target]

    I1 --> J[Invoke Node Executor]
    I2 --> J
    I3 --> J

    J --> K{Async Node?}

    K -- Yes --> L[Register Callback]
    L --> M[Wait for Callback/Event]
    M --> N[Update Token State]

    K -- No --> N[Update Token State]

    N --> O[Persist State Snapshot]
    O --> P[Write Execution History]

    P --> Q{Error Occurred?}

    Q -- Yes --> R[Error Handler]
    R --> S{Retry Policy?}

    S -- Retry --> J
    S -- Compensate --> T[Run Compensation Node]
    S -- Fail --> U[Mark Workflow Failed]

    Q -- No --> V[Emit Success Event]

    V --> E
```

---

## 🔁 Composite Node (Sub-Workflow) — Sequence Diagram

```mermaid
sequenceDiagram
    participant ORCH as Orchestrator
    participant SUB as Sub-Workflow Manager
    participant REG as Executor Registry
    participant EX as Executor

    ORCH ->> SUB: start(subWorkflowId)
    SUB ->> SUB: create execution token
    loop sub-workflow nodes
        SUB ->> REG: select executor
        REG -->> SUB: executor target
        SUB ->> EX: dispatchTask
        EX -->> SUB: taskResult
        SUB ->> SUB: persist + next node
    end
    SUB -->> ORCH: sub-workflow result
```

## 🧠 How to read this

Main loop:

```
Trigger → Token → Node → Route → Execute → Persist → Next Node
```

---

### 🔹 Important control points

**Routing decision**

```
Routing Policy
```

**Async vs Sync**

```
Async Node?
```

**Error strategy**

```
Retry / Compensate / Fail
```

---

### 🔹 This supports

✔ BPMN-style flows
✔ EIP patterns
✔ Agent chains
✔ Fan-out / fan-in
✔ Human-in-the-loop (via callbacks)
✔ Long-running workflows






---

## 🔄 Gamelan Workflow Execution — State Machine

```mermaid
stateDiagram-v2
    [*] --> Created

    Created --> Initialized : load definition
    Initialized --> Running : start execution

    Running --> Waiting : async node / callback
    Waiting --> Running : callback received

    Running --> Retrying : node error & retry policy
    Retrying --> Running : retry success

    Running --> Compensating : compensation required
    Compensating --> Running : compensation done

    Running --> Suspended : manual pause / human-in-loop
    Suspended --> Running : resume

    Running --> Failed : unrecoverable error
    Running --> Completed : no more nodes

    Failed --> [*]
    Completed --> [*]
```

---

## 🧠 How to read this

**Main happy path**

```
Created → Initialized → Running → Completed
```

**Async path**

```
Running → Waiting → Running
```

**Failure handling**

```
Running → Retrying → Running
Running → Compensating → Running
Running → Failed
```

**Human control**

```
Running → Suspended → Running
```

---

## 🎯 Features

✔ Formal execution semantics
✔ Deterministic lifecycle
✔ Clear recovery points
✔ Support for long-running workflows
✔ Human-in-the-loop
✔ Compensation (Saga pattern)
✔ Retry & backoff
✔ Pausing & resuming


---

## 🧭 Current Implementation Mapping (Repo)

* **Gamelan Engine** → `workflow-gamelan/core/gamelan-engine`
* **Executor Registry** → `workflow-gamelan/core/gamelan-executor-registry`
* **Runtime API (REST/gRPC/Kafka)** → `workflow-gamelan/core/gamelan-runtime-core`, `workflow-gamelan/protocol/`
* **SDK Client** → `workflow-gamelan/sdk/gamelan-sdk-client-*`
* **Executor SDKs** → `workflow-gamelan/sdk/gamelan-sdk-executor-*`

---

## 🧩 Gamelan SDK Mechanism (Client + Executor)

```mermaid
flowchart TB
    subgraph SDKC["Client SDK"]
        CLocal["Local Client SDK"]
        CRemote["Remote Client SDK"]
        CGrpc["Remote Transport: gRPC"]
        CKafka["Remote Transport: Kafka"]
        CRest["Remote Transport: REST"]
    end

    subgraph SDKE["Executor SDK"]
        ELocal["Local Executor SDK"]
        ERemote["Remote Executor SDK"]
        EGrpc["Remote Transport: gRPC"]
        EKafka["Remote Transport: Kafka"]
        ERest["Remote Transport: REST"]
    end

    subgraph CORE["Gamelan Core"]
        Engine["Gamelan Engine"]
        Runtime["Gamelan Runtime API"]
        Registry["Executor Registry"]
    end

    CLocal --> Engine
    CRemote --> Runtime
    CRemote --> CGrpc
    CRemote --> CKafka
    CRemote --> CRest
    CGrpc --> Runtime
    CKafka --> Runtime
    CRest --> Runtime

    ELocal --> Engine
    ERemote --> Registry
    ERemote --> Runtime
    ERemote --> EGrpc
    ERemote --> EKafka
    ERemote --> ERest
    EGrpc --> Runtime
    EKafka --> Runtime
    ERest --> Runtime
```

---

## 🔁 Gamelan Execution — Sequence Diagram

```mermaid
sequenceDiagram
    participant RT as Gamelan Runtime API
    participant ORCH as Orchestrator
    participant REG as Executor Registry
    participant EX as Executor

    RT ->> ORCH: submitWorkflow
    ORCH ->> ORCH: schedule node
    ORCH ->> REG: select executor
    REG -->> ORCH: executor target
    ORCH ->> EX: dispatchTask
    EX -->> ORCH: taskResult(status, output)
    alt task error/timeout
        ORCH ->> ORCH: retry/compensate/fail
        ORCH -->> RT: event(node.failed)
    else success
        ORCH ->> ORCH: persist + next node
        ORCH -->> RT: event(node.completed)
    end
```
