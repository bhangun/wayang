
---

## ✅  BIG PICTURE ARCHITECTURE 

```mermaid
flowchart TB

%% =========================
%% UI
%% =========================
UI["Wayang Studio / UI"]

%% =========================
%% WAYANG
%% =========================
subgraph WAYANG["Wayang Platform"]
    subgraph CP["Wayang Control Plane"]
        Registry["Agent & Tool Registry"]
        Policy["Routing & Policy Engine"]
        Secrets["Secrets / Config"]
        API["Wayang API"]
    end

    subgraph RT["Wayang Runtime"]
        Router["Task Router"]
        Semantic["Node Semantic Resolver"]
        GamelanClient["Gamelan Client SDK"]
    end
end

%% =========================
%% GAMELAN
%% =========================
subgraph GAMELAN["Gamelan Workflow Engine"]
    Engine["Workflow Engine"]
    Scheduler["Node Scheduler"]
    Dispatcher["Task Dispatcher"]
    State["State & Persistence"]
    ExecRegistry["Executor Registry"]
    Discovery["Service Discovery Adapter"]
end

%% =========================
%% EXECUTORS
%% =========================
subgraph EXECUTORS["Executor Runtimes"]
    subgraph JAVA["Java Agent Runtime (Quarkus)"]
        Planner["Planner Agent"]
        Orchestrator["Orchestrator Agent"]
        Evaluator["Evaluator Agent"]
    end

    subgraph PY["Python Agent Runtime"]
        ML["ML/NLP Agent"]
    end

    subgraph COMMERCE["Commerce Agent Runtime (Future)"]
        Commerce["Commerce Agent"]
    end
end

%% =========================
%% GOLEK
%% =========================
subgraph GOLEK["Golek Inference Engine"]
    ModelReg["Model Registry"]
    Inference["Golek-Engine"]
    Backends["GPU/CPU/Cloud Backends Provider"]
end

%% =========================
%% INFRA
%% =========================
subgraph INFRA["Infrastructure"]
    Kafka["Kafka / Event Bus"]
    DB["Postgres / Storage"]
    Redis["Redis"]
    Obs["Observability"]
end

%% =========================
%% FLOWS
%% =========================
UI --> API

API --> Registry
API --> Policy
API --> Secrets
API --> Router

Registry --> Semantic
Policy --> Router
Semantic --> Router

Router --> GamelanClient
GamelanClient --> Engine

Engine --> Scheduler
Scheduler --> Dispatcher
Dispatcher --> ExecRegistry
ExecRegistry --> Discovery

Discovery --> Planner
Discovery --> Orchestrator
Discovery --> Evaluator
Discovery --> ML
Discovery --> Commerce

Planner --> Dispatcher
Orchestrator --> Dispatcher
Evaluator --> Dispatcher
ML --> Dispatcher
Commerce --> Dispatcher

Dispatcher --> Engine
Engine --> State

%% Events back to Wayang
Engine --> Kafka
Kafka --> API

%% Inference path
Planner --> Inference
Orchestrator --> Inference
ML --> Inference
Inference --> ModelReg
Inference --> Backends

%% Infra usage
Engine --> DB
Engine --> Redis
Engine --> Obs
GOLEK --> Obs
WAYANG --> Obs
```

---

## 🧠 Core Rules Embedded in This Diagram

* **Executors NEVER talk to Wayang directly**
* **All results go:**

  ```
  Executor → Gamelan → Wayang
  ```
* **Wayang = semantic brain**
* **Gamelan = execution brain**
* **Golek = inference brain**

---

Alright, next step: let’s make the **runtime behavior crystal clear** with a
👉 **SEQUENCE DIAGRAM (Executor → Gamelan → Wayang)**

This answers your earlier confusion about:

* who sends result
* who sends status
* who notifies UI

---

## ✅ SEQUENCE DIAGRAM — NORMAL EXECUTION

```mermaid
sequenceDiagram
    participant UI as Wayang UI
    participant CP as Wayang Control Plane
    participant RT as Wayang Runtime
    participant GE as Gamelan Engine
    participant EX as Executor Runtime
    participant GO as Golek Inference

    UI ->> CP: Deploy workflow
    CP ->> RT: Start workflow
    RT ->> GE: submitWorkflow(definition)

    GE ->> GE: schedule node
    GE ->> EX: dispatchTask(node, payload)

    EX ->> GO: infer(prompt)
    GO -->> EX: inference result

    EX -->> GE: taskResult(status=SUCCESS, output)

    GE ->> GE: persist state
    GE ->> GE: decide next node

    GE -->> CP: event(node.completed, result)
    CP -->> UI: update status + result

    GE ->> EX: dispatchTask(nextNode)
```

---

## ❌ WHAT DOES NOT HAPPEN

```
Executor ──X──► Wayang   (NO)
Executor ──X──► UI       (NO)
```

Executor ONLY talks to:

```
Executor → Gamelan
```

---

## 🟥 FAILURE & RETRY SCENARIO

```mermaid
sequenceDiagram
    participant RT as Wayang Runtime
    participant GE as Gamelan Engine
    participant EX as Executor

    RT ->> GE: submitWorkflow
    GE ->> EX: dispatchTask

    EX -->> GE: taskResult(status=FAIL, error)

    GE ->> GE: retry policy?
    alt retry allowed
        GE ->> EX: dispatchTask (retry)
    else no retry
        GE -->> RT: event(node.failed)
    end
```

Wayang:

* sees failure
* updates UI
* may request human intervention
* but does NOT retry itself

---

## 🟡 STREAMING (chat / HITL case)

Optional side-channel:

```mermaid
sequenceDiagram
    participant EX as Executor
    participant GE as Gamelan
    participant CP as Wayang Control Plane
    participant UI as UI

    EX -->> GE: partialOutput(chunk)
    EX -->> CP: stream(chunk)  %% optional UI stream

    GE -->> CP: node.completed(finalResult)
    CP -->> UI: final result
```

Authority is still:

```
final result = from Gamelan
```

---

## 🧠 FINAL RULES (LOCK THESE)

1. **Executor → Gamelan** = result + status
2. **Gamelan → Wayang** = state + semantic result
3. **Wayang → UI** = visualization + policy
4. **Retries live in Gamelan**
5. **Routing lives in Wayang**
6. **Inference lives in Golek**

---

---

## 🔁 Wayang → Gamelan → Golek — Combined Flowchart

```mermaid
flowchart TD
    %% ========== WAYANG ==========
    A[User / Designer] --> B[Wayang API]

    B --> C{Authenticated?}
    C -- No --> C1[Reject]
    C -- Yes --> D[Workflow / Agent Manager]

    D --> E[Schema Registry Validation]
    E --> F{Schema Valid?}
    F -- No --> F1[Return Validation Error]
    F -- Yes --> G[Version & Package Workflow]

    G --> H[Deployment Manager]
    H --> I[Runtime Registry]

    I --> J{Gamelan Runtime Available?}
    J -- No --> J1[Deployment Failed]
    J -- Yes --> K[Deploy Workflow to Gamelan]

    %% ========== GAMELAN ==========
    K --> L[Receive Workflow Trigger]

    L --> M[Load Workflow Definition]
    M --> N[Create Execution Token]
    N --> O[Persist Initial State]

    O --> P{Next Node?}
    P -- No --> Z[Workflow Completed]
    P -- Yes --> Q[Lookup Node Executor]

    Q --> R[Task Router]
    R --> S{Routing Policy}
    S --> S1[Select Target Executor]

    S1 --> T[Invoke Node Executor]

    T --> U{Node Needs Inference?}

    %% ========== GOLEK ==========
    U -- Yes --> V[Call Golek Inference API]

    V --> W[Auth & Quota Check]
    W --> X{Authorized?}
    X -- No --> X1[Reject Inference]
    X -- Yes --> Y[Resolve Model & Runtime]

    Y --> AA["Build Context (Prompt/Tools/Memory)"]
    AA --> AB[Check Cache]

    AB --> AC{Cache Hit?}
    AC -- Yes --> AD[Return Cached Result]
    AC -- No --> AE[Run Model Inference]

    AE --> AF{Error?}
    AF -- Yes --> AG[Error Handler & Fallback]
    AF -- No --> AH[Store Result in Cache]

    AH --> AI[Return Inference Result]

    %% ========== BACK TO GAMELAN ==========
    AD --> AJ[Update Token State]
    AI --> AJ

    AJ --> AK[Persist State Snapshot]
    AK --> AL[Write Execution History]

    AL --> AM{Error in Node?}
    AM -- Yes --> AN[Retry / Compensate / Fail]
    AM -- No --> AO[Emit Success Event]

    AO --> P
```

---

## 🧠 Mental model

### Wayang (Control Plane)

```
Design → Validate → Package → Deploy
```

### Gamelan (Execution Plane)

```
Token → Node → Route → Execute → Persist → Loop
```

### Golek (Inference Plane)

```
Auth → Model → Context → Infer → Cache → Return
```

---

## 🧩 What this diagram proves architecturally

✔ Clean separation of concerns
✔ Works in microservices OR single-JVM
✔ Supports retries, fallbacks, and async
✔ Multi-model & multi-provider
✔ Fully agentic + BPMN + EIP compatible

This is a **very strong architecture story** for:

* docs
* investors
* OSS README
* thesis / paper
* conference talk

---

