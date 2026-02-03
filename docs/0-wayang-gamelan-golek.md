
---

## ✅  BIG PICTURE ARCHITECTURE 

```mermaid
flowchart TB

%% =========================
%% UI + Gateway Edge
%% =========================
UI["Wayang Studio / UI"]
GW["Iket Gateway (API Edge)"]

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
    GamelanAPI["Gamelan API"]
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
    subgraph AGENT_POOL["Agent Pool (Java / Quarkus)"]
        Planner["Planner Agent"]
        Orchestrator["Orchestrator Agent"]
        Evaluator["Evaluator Agent"]
        Guardrails["Rules / Guardrails"]
    end

    subgraph RAGX["RAG Runtime (Java)"]
        RAG["RAG Executor"]
    end

    subgraph EIPX["EIP / Integration Runtime (Java)"]
        EIP["EIP / Integration Executor"]
    end

    subgraph PY["Python Agent Runtime (Future)"]
        ML["ML/NLP Agent (Future)"]
    end

    subgraph COMMERCE["Commerce Agent Runtime (Future)"]
        Commerce["Commerce Agent"]
    end
end

%% =========================
%% GOLEK
%% =========================
subgraph GOLEK["Golek Inference Engine"]
    GolekAPI["Golek API"]
    ModelReg["Model Registry"]
    Inference["Golek-Engine"]
    Backends["GPU/CPU/Cloud Backends Provider"]
end

%% =========================
%% MCP (Tools Plane)
%% =========================
subgraph MCP["MCP Tool Plane"]
    MCPAPI["MCP API"]
    MCPServer["MCP Server"]
    MCPRegistry["Tool Registry"]
    MCPExec["Tool Execution"]
end

%% =========================
%% EXTERNAL SYSTEMS
%% =========================
subgraph EXTERNAL["External Providers / Systems"]
    LLMProviders["LLM Providers (OpenAI, Gemini, Anthropic, etc.)"]
    ExtAPIs["External APIs / OpenAPI Services"]
    ExtDBs["External Databases / SaaS"]
    ExtKB["External Knowledge / Content Sources"]
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
UI --> GW
GW --> API

API --> Registry
API --> Policy
API --> Secrets
API --> Router

Registry --> Semantic
Policy --> Router
Semantic --> Router

Router --> GamelanClient
GamelanClient --> GamelanAPI
GamelanAPI --> Engine

Engine --> Scheduler
Scheduler --> Dispatcher
Dispatcher --> ExecRegistry
ExecRegistry --> Discovery

Discovery --> Planner
Discovery --> Orchestrator
Discovery --> Evaluator
Discovery --> RAG
Discovery --> EIP
Discovery --> ML
Discovery --> Commerce

Planner --> Dispatcher
Orchestrator --> Dispatcher
Evaluator --> Dispatcher
RAG --> Dispatcher
EIP --> Dispatcher
ML --> Dispatcher
Commerce --> Dispatcher

Dispatcher --> Engine
Engine --> State

%% Events back to Wayang
Engine --> Kafka
Kafka --> API

%% Inference path
Planner --> GolekAPI
Orchestrator --> GolekAPI
ML --> GolekAPI
GolekAPI --> Inference
Inference --> ModelReg
Inference --> Backends
Backends --> LLMProviders

%% MCP usage
Planner --> MCPAPI
Orchestrator --> MCPAPI
Evaluator --> MCPAPI
MCPAPI --> MCPServer
MCPServer --> MCPRegistry
MCPServer --> MCPExec
MCPExec --> ExtAPIs
MCPExec --> ExtDBs

Guardrails --> Planner
Guardrails --> Orchestrator
Guardrails --> Evaluator

%% RAG / EIP external usage
RAG --> ExtKB
RAG --> ExtDBs
EIP --> ExtAPIs
EIP --> ExtDBs

%% Infra usage
Engine --> DB
Engine --> Redis
Engine --> Obs
GOLEK --> Obs
WAYANG --> Obs
```

---

## ✅ BIG PICTURE (Standalone / Single-JVM Mode)

All core modules run in the same JVM process for minimal latency and simpler deployment.

```mermaid
flowchart TB

%% =========================
%% Standalone JVM
%% =========================
subgraph JVM["Standalone JVM (All Modules In-Process)"]
    subgraph WAYANG_S["Wayang (Control + Runtime)"]
        API_S["Wayang API"]
        Registry_S["Agent & Tool Registry"]
        Policy_S["Routing & Policy Engine"]
        Router_S["Task Router"]
        Semantic_S["Node Semantic Resolver"]
        GamelanClient_S["Gamelan Client SDK (Local)"]
    end

    subgraph GAMELAN_S["Gamelan Workflow Engine"]
        GamelanAPI_S["Gamelan API (Local)"]
        Engine_S["Workflow Engine"]
        Scheduler_S["Node Scheduler"]
        Dispatcher_S["Task Dispatcher"]
        ExecRegistry_S["Executor Registry"]
    end

subgraph EXEC_S["Executor Runtimes (Java)"]
    subgraph AGENT_POOL_S["Agent Pool (Java / Quarkus)"]
        Planner_S["Planner Agent"]
        Orchestrator_S["Orchestrator Agent"]
        Evaluator_S["Evaluator Agent"]
    end
    RAG_S["RAG Executor"]
    EIP_S["EIP / Integration Executor"]
end

    subgraph GOLEK_S["Golek Inference Engine"]
        GolekAPI_S["Golek API (Local)"]
        Inference_S["Golek-Engine"]
        ModelReg_S["Model Registry"]
    end

subgraph MCP_S["MCP Tool Plane"]
    MCPAPI_S["MCP API (Local)"]
    MCPServer_S["MCP Server"]
    MCPExec_S["Tool Execution"]
end
end

%% =========================
%% External Infra (Optional)
%% =========================
subgraph INFRA_S["External Infrastructure (Optional)"]
    Kafka_S["Kafka / Event Bus"]
    DB_S["Postgres / Storage"]
    Redis_S["Redis"]
    Obs_S["Observability"]
end

%% =========================
%% External Providers / Systems
%% =========================
subgraph EXTERNAL_S["External Providers / Systems"]
    LLMProviders_S["LLM Providers (OpenAI, Gemini, Anthropic, etc.)"]
    ExtAPIs_S["External APIs / OpenAPI Services"]
    ExtDBs_S["External Databases / SaaS"]
    ExtKB_S["External Knowledge / Content Sources"]
end

API_S --> Registry_S
API_S --> Policy_S
API_S --> Router_S
Registry_S --> Semantic_S
Semantic_S --> Router_S
Router_S --> GamelanClient_S
GamelanClient_S --> GamelanAPI_S
GamelanAPI_S --> Engine_S
Engine_S --> Scheduler_S
Scheduler_S --> Dispatcher_S
Dispatcher_S --> ExecRegistry_S
ExecRegistry_S --> Planner_S
ExecRegistry_S --> Orchestrator_S
ExecRegistry_S --> Evaluator_S
ExecRegistry_S --> RAG_S
ExecRegistry_S --> EIP_S

Planner_S --> GolekAPI_S
Orchestrator_S --> GolekAPI_S
GolekAPI_S --> Inference_S
Inference_S --> ModelReg_S
Inference_S --> LLMProviders_S

Planner_S --> MCPAPI_S
Orchestrator_S --> MCPAPI_S
Evaluator_S --> MCPAPI_S
MCPAPI_S --> MCPServer_S
MCPServer_S --> MCPExec_S
MCPExec_S --> ExtAPIs_S
MCPExec_S --> ExtDBs_S

RAG_S --> ExtKB_S
RAG_S --> ExtDBs_S
EIP_S --> ExtAPIs_S
EIP_S --> ExtDBs_S

Engine_S --> DB_S
Engine_S --> Redis_S
Engine_S --> Kafka_S
WAYANG_S --> Obs_S
GAMELAN_S --> Obs_S
GOLEK_S --> Obs_S
MCP_S --> Obs_S
```

Notes:
* In standalone mode, APIs are local interfaces (no network hop).
* Gateway (Iket) can be optional or used as an external edge if needed.
* External infra can be embedded or mocked for dev, but is typically external in prod.

---

## 🗺️ Legend

* **(Future)** = planned but not yet implemented in this repo

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
* **Iket = gateway edge**
* **MCP = tool plane**

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
7. **API edge lives in Iket**
8. **Tools live in MCP**

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

---

## 🧭 Current Implementation Mapping (Repo)

This section ties diagram nodes to concrete folders in this repo so the diagram stays grounded in code.

* **Wayang UI** → `wayang-ui/` (designer, admin, CLI, core UI libs)
* **Iket Gateway** → `gateway-iket/` (Go gateway + enterprise variant + admin UI)
* **Wayang Control Plane** → `wayang/core/wayang-control-plane-core`
* **Wayang Orchestrator / Gamelan Client** → `wayang/core/wayang-orchestrator-*`
* **Gamelan API** → `workflow-gamelan/core/gamelan-runtime-core`, `workflow-gamelan/protocol/*`
* **Gamelan Engine** → `workflow-gamelan/core/gamelan-engine`
* **Gamelan Registry & Runtime** → `workflow-gamelan/core/gamelan-executor-registry`, `workflow-gamelan/core/gamelan-runtime-core`
* **Executor Runtimes (Java)** → `wayang/executors/` (agent, rag, tool, eip, guardrails, memory, vector, etc.)
* **Golek API / Inference Engine** → `inference-golek/` (core, provider, adapter, runtime)
* **MCP API / Tool Plane** → `mcp-kulit/`

This is a **very strong architecture story** for:

* docs
* investors
* OSS README
* thesis / paper
* conference talk

---

## 🧩 Complex Cases

### 1) Composite Node (Sub-Workflow / Project as a Node)

```mermaid
flowchart TB
    subgraph WF["Workflow: Parent"]
        A["Node A"]
        B["Composite Node: Sub-Workflow / Project"]
        C["Node C"]
    end

    subgraph SUB["Sub-Workflow (Composite Node)"]
        S1["Sub-Node 1"]
        S2["Sub-Node 2"]
        S3["Sub-Node 3"]
    end

    A --> B --> C
    B --> S1
    S1 --> S2 --> S3
```

### 2) Agent Pool Coordination (Orchestrator + Agent-to-Agent)

```mermaid
flowchart TB
    subgraph EXEC["Executor: Agent Pool Node"]
        Orch["Orchestrator Agent"]
        A1["Agent A"]
        A2["Agent B"]
        A3["Agent C"]
        Rules["Rules / Guardrails"]
        Bus["Agent Comms Bus"]
    end

    Orch --> Rules
    Rules --> Bus
    Orch --> Bus
    Bus <--> A1
    Bus <--> A2
    Bus <--> A3
    Orch --> A1
    Orch --> A2
    Orch --> A3
```

### 3) Tool Invocation (Agent → MCP → External System)

```mermaid
sequenceDiagram
    participant AG as Agent
    participant GR as Guardrails/Policy
    participant MCP as MCP API
    participant REG as Tool Registry
    participant EXT as External API/DB

    AG ->> GR: tool request
    GR -->> AG: allowed/denied
    alt denied
        AG -->> AG: abort tool call
    else allowed
        AG ->> MCP: invokeTool(name, input)
        MCP ->> REG: resolve tool
        REG -->> MCP: tool endpoint
        MCP ->> EXT: execute call
        EXT -->> MCP: result
        MCP -->> AG: tool result
    end
```
