# WAYANG AGENTIC AI BUILDER



## Wayang family

* Users + UI
* Iket Gateway (API Edge)
* Wayang Control Plane
* MCP Tool Plane
* Gamelan Workflow Engine
* Executor Runtimes (Agent Pool, RAG, EIP)
* Golek Inference Server
* Model Providers (cloud & local)
* External Systems (APIs, DBs, Knowledge)
* Storage + Messaging


```mermaid
C4Container
title Wayang Family Architecture (Wayang + Iket + MCP + Gamelan + Golek)

Person(user, "User / Developer", "Designs and runs agentic workflows")

System_Boundary(wayang, "Wayang Platform") {

    Container(ui, "Wayang Designer UI", "Flutter/Web", "Low-code workflow & agent designer")

    Container(control, "Wayang Control Plane", "Quarkus", "Manages projects, plugins, secrets, and workflow definitions")

    Container(api, "Wayang API", "REST/gRPC", "Control plane API for UI and clients")

    ContainerDb(meta, "Wayang Metadata DB", "PostgreSQL", "Stores workflows, agents, schemas, configs")
}

System_Boundary(iket, "Iket Gateway (API Edge)") {
    Container(gateway, "Iket Gateway", "Go", "API edge, routing, auth, rate limiting")
}

System_Boundary(gamelan, "Gamelan Workflow Engine") {
    Container(orchestrator, "Gamelan Orchestrator", "Quarkus", "Executes workflows, routes tasks, manages state")
    Container(executorSel, "Executor Selection", "Java", "Selects executor targets from registry")
    ContainerDb(state, "Execution State Store", "Redis / DB", "Workflow runtime state")
}

System_Boundary(executors, "Executor Runtimes") {
    Container(agentPool, "Agent Pool", "Quarkus / Java", "Planner, Orchestrator, Evaluator agents")
    Container(ragExec, "RAG Executor", "Java", "Retrieval & context building")
    Container(eipExec, "EIP / Integration Executor", "Java", "Integration patterns & connectors")
}

System_Boundary(mcp, "MCP Tool Plane") {
    Container(mcpServer, "MCP Server", "Java", "Tool registry & execution")
}

System_Boundary(golek, "Golek Inference Server") {
    Container(inference, "Inference API", "Python/Java", "Unified LLM & model inference interface")
    Container(modelmgr, "Model Manager", "Runtime", "Loads and manages models")
}

System_Boundary(models, "Model Providers") {
    Container(cloud, "Cloud LLM Providers", "OpenAI / Gemini / Anthropic", "External APIs")
    Container(local, "Local Model Runtime", "Ollama / LiteRT / vLLM", "On-prem inference")
}

System_Boundary(ext, "External Systems") {
    Container(extApi, "External APIs", "HTTP/OpenAPI", "SaaS and partner APIs")
    Container(extDb, "External DB / Knowledge", "DB / Search", "RAG sources and data stores")
}

Rel(user, ui, "Uses")
Rel(ui, gateway, "Calls")
Rel(gateway, api, "Routes requests")
Rel(api, control, "Handles control plane")
Rel(control, meta, "Reads/Writes")

Rel(control, orchestrator, "Deploys & triggers workflows", "gRPC/REST")
Rel(orchestrator, executorSel, "Selects executor")
Rel(orchestrator, state, "Persists state")

Rel(orchestrator, inference, "Requests inference", "gRPC/HTTP")
Rel(inference, modelmgr, "Loads & runs models")

Rel(modelmgr, cloud, "Calls")
Rel(modelmgr, local, "Executes")

Rel(orchestrator, agentPool, "Dispatches tasks")
Rel(orchestrator, ragExec, "Dispatches tasks")
Rel(orchestrator, eipExec, "Dispatches tasks")

Rel(agentPool, mcpServer, "Tool calls")
Rel(ragExec, extDb, "Retrieves knowledge")
Rel(eipExec, extApi, "Integrates")

Rel(orchestrator, api, "Emits events & results")
Rel(api, ui, "Streams results")
```

---

### How this maps to your product vision

**Wayang = Control Plane**

* UI designer
* Plugin system
* Workflow definition (`.wy`)
* Secrets & schema registry
* Runs behind Iket Gateway (API edge)

**Gamelan = Data Plane / Orchestration**

* Executes nodes
* Executor selection & dispatch
* Retry, circuit breaker, compensation
* Multi-executor (agent, BPMN, EIP, NLP, time-series)

**MCP = Tool Plane**

* Tool registry & execution
* Shared tool surface for agents, RAG, and integrations

**Golek = Inference Plane**

* LLM + ML inference
* Multi-provider abstraction
* Local & cloud models
* Future: batching, KV cache, LoRA, quantized models

---


### Wayang Internal Modules

```mermaid
C4Component
title Wayang Platform - Internal Architecture (Control Plane)

Container(wayang, "Wayang Platform", "Quarkus / Java", "Low-code control plane for agentic workflow system")

Component(api, "Wayang API", "REST/gRPC", "Public API for UI, CLI and automation")

Component(auth, "Auth & Identity", "Keycloak / OIDC", "Users, roles, tenants, tokens")

Component(projectMgr, "Project Manager", "Core Service", "Manages workspaces and projects")

Component(workflowMgr, "Workflow Manager", "Core Service", "CRUD for workflow definitions (.wy)")

Component(agentMgr, "Agent Manager", "Core Service", "Agent definitions, tools, memory schemas")

Component(schemaRegistry, "Schema Registry", "Metadata", "Tool, memory, resource, function schemas")

Component(pluginMgr, "Plugin Manager", "SPI", "Loads and manages executor, tool and provider plugins")

Component(secretMgr, "Secret Manager", "Vault / KMS", "Stores API keys, credentials and tokens")

Component(configMgr, "Config Manager", "Core Service", "Profiles, environments and runtime configs")

Component(deployMgr, "Deployment Manager", "Control Plane", "Deploys workflows to Gamelan runtimes")

Component(runtimeRegistry, "Runtime Registry", "Service Registry", "Tracks available Gamelan & Golek runtimes")

Component(versionMgr, "Versioning & Packaging", "Artifact Service", "Workflow versions, bundles, .wy packages")

Component(eventBus, "Control Plane Event Bus", "Kafka / In-Memory", "Lifecycle & audit events")

Component(auditLog, "Audit Log", "Compliance", "Tracks user & system actions")

Component(uiBackend, "Designer Backend", "BFF", "Backend-for-frontend for UI")

Component(observability, "Metrics & Tracing", "OpenTelemetry", "System observability")

Rel(api, auth, "Authenticates with")
Rel(api, projectMgr, "Manages projects")
Rel(api, workflowMgr, "Manages workflows")
Rel(api, agentMgr, "Manages agents")

Rel(workflowMgr, schemaRegistry, "Validates against")
Rel(agentMgr, schemaRegistry, "Validates against")

Rel(pluginMgr, schemaRegistry, "Registers schemas")
Rel(pluginMgr, runtimeRegistry, "Registers capabilities")

Rel(api, secretMgr, "Stores & reads secrets")
Rel(api, configMgr, "Reads configs")

Rel(deployMgr, runtimeRegistry, "Selects runtime from")
Rel(deployMgr, workflowMgr, "Packages workflows from")

Rel(deployMgr, eventBus, "Emits deployment events")
Rel(eventBus, auditLog, "Persists")

Rel(api, uiBackend, "Serves UI data")
Rel(api, observability, "Emits metrics")
Rel(deployMgr, observability, "Emits metrics")

```






### Wayang Flowchart

```mermaid
flowchart TD
    A[User / UI Request] --> GW[Iket Gateway]
    GW --> B[Wayang API]

    B --> C{Authenticated?}
    C -- No --> C1[Reject Request]
    C -- Yes --> D[Project Manager]

    D --> E{Request Type?}

    E -- Workflow CRUD --> F[Workflow Manager]
    E -- Agent CRUD --> G[Agent Manager]
    E -- Plugin Ops --> H[Plugin Manager]
    E -- Secrets --> I[Secret Manager]
    E -- Config --> J[Config Manager]
    E -- Deploy --> K[Deployment Manager]

    F --> L[Schema Registry]
    G --> L

    L --> M{Schema Valid?}
    M -- No --> M1[Return Validation Error]
    M -- Yes --> N[Versioning & Packaging]

    N --> O[Runtime Registry]
    O --> P{Runtime Available?}

    P -- No --> P1[Deployment Failed]
    P -- Yes --> Q[Deploy to Gamelan Runtime]

    Q --> R[Emit Control Event]

    R --> S[Audit Log]
    R --> T[Observability / Metrics]

    H --> L
    K --> O

```



## Wayang Deployment Standalone Mode


- ✅ Wayang + Gamelan + Golek
- ✅ All run **inside one JVM process**
- ✅ No network hops
- ✅ Uses embedded DB & local models
- ✅ Ideal for: dev, desktop, edge, offline

---

### 🧩 Wayang Family — Standalone / Local (Single JVM)

```mermaid
C4Container
title Wayang Family - Standalone / Local Mode (Single JVM)

Person(user, "User / Developer", "Designs and runs workflows locally")

System_Boundary(jvm, "Single JVM Runtime (Standalone Mode)") {

    Container(ui, "Wayang Designer UI", "Flutter/Desktop or WebView", "Local workflow & agent designer")

    Container(core, "Wayang Core", "Java (Quarkus)", "Control plane, plugins, secrets, schemas")

    Container(workflow, "Gamelan Engine", "Java", "Workflow orchestration & execution")

    Container(inference, "Golek Inference", "Java/Python embedded", "Local inference abstraction")

    Container(modelmgr, "Model Manager", "Runtime", "Loads local models")

    ContainerDb(meta, "Embedded Metadata Store", "H2/SQLite", "Workflows, agents, configs")

    ContainerDb(state, "Embedded State Store", "RocksDB/MapDB", "Execution state & cache")

    Container(localModels, "Local Models", "GGUF / ONNX / LiteRT", "On-device models")
}

Rel(user, ui, "Uses")
Rel(ui, core, "Calls (in-process)")
Rel(core, meta, "Reads/Writes")

Rel(core, workflow, "Triggers workflows")
Rel(workflow, state, "Persists state")

Rel(workflow, inference, "Requests inference (in-process)")
Rel(inference, modelmgr, "Loads models")
Rel(modelmgr, localModels, "Executes")

Rel(workflow, ui, "Returns results")
```

---

## 🧠 Key Architectural Meaning

### 🔹 Single Process

Everything runs inside:

```
wayang-standalone.jar
```

No:
- ❌ Kubernetes
- ❌ gRPC
- ❌ API Gateway
- ❌ External DB

---

### 🔹 Embedded Replacements

| Distributed Mode | Standalone Mode     |
| ---------------- | ------------------- |
| PostgreSQL       | H2 / SQLite         |
| Redis            | RocksDB / MapDB     |
| Golek service    | In-process lib      |
| REST/gRPC        | Direct method calls |
| Ollama/vLLM      | LiteRT / ONNX / JNI |

---

### 🔹 Use cases

✅ Local workflow testing
✅ Desktop AI apps
✅ Edge devices
✅ Offline agent runtime
✅ Developer mode
✅ “Portable Wayang” (USB / ZIP runtime)

---



## Wayang Deployment Cloud Mode

```mermaid

C4Container
title Wayang Family Architecture (Wayang + Gamelan + Golek)

Person(user, "User / Developer", "Designs and runs agentic workflows")

System_Boundary(wayang, "Wayang Platform") {

    Container(ui, "Wayang Designer UI", "Flutter/Web", "Low-code workflow & agent designer")

    Container(control, "Wayang Control Plane", "Quarkus", "Manages projects, plugins, secrets, and workflow definitions")

    Container(api, "Wayang API Gateway", "REST/gRPC", "Single entry point for UI and external clients")

    ContainerDb(meta, "Wayang Metadata DB", "PostgreSQL", "Stores workflows, agents, schemas, configs")
}

System_Boundary(gamelan, "Gamelan Workflow Engine") {
    Container(orchestrator, "Gamelan Orchestrator", "Quarkus", "Executes workflows, routes tasks, manages state")
    Container(router, "Task Router", "Java", "Selects executor & inference targets")
    ContainerDb(state, "Execution State Store", "Redis / DB", "Workflow runtime state")
}

System_Boundary(golek, "Golek Inference Server") {
    Container(inference, "Inference API", "Python/Java", "Unified LLM & model inference interface")
    Container(modelmgr, "Model Manager", "Runtime", "Loads and manages models")
}

System_Boundary(models, "Model Providers") {
    Container(cloud, "Cloud LLM Providers", "OpenAI / Gemini / Anthropic", "External APIs")
    Container(local, "Local Model Runtime", "Ollama / LiteRT / vLLM", "On-prem inference")
}

Rel(user, ui, "Uses")
Rel(ui, api, "Calls")
Rel(api, control, "Routes requests")
Rel(control, meta, "Reads/Writes")

Rel(control, orchestrator, "Deploys & triggers workflows", "gRPC/REST")
Rel(orchestrator, router, "Asks for routing decision")
Rel(orchestrator, state, "Persists state")

Rel(orchestrator, inference, "Requests inference", "gRPC/HTTP")
Rel(inference, modelmgr, "Loads & runs models")

Rel(modelmgr, cloud, "Calls")
Rel(modelmgr, local, "Executes")

Rel(orchestrator, api, "Emits events & results")
Rel(api, ui, "Streams results")

```

---

## 🧭 Current Implementation Mapping (Repo)

* **Wayang UI** → `wayang-ui/`
* **Iket Gateway** → `gateway-iket/`
* **Wayang Control Plane** → `wayang/core/wayang-control-plane-core`
* **Wayang Orchestrator / Gamelan Client** → `wayang/core/wayang-orchestrator-*`
* **MCP Tool Plane** → `mcp-kulit/`
* **Gamelan Engine** → `workflow-gamelan/core/gamelan-engine`
* **Gamelan Runtime API** → `workflow-gamelan/core/gamelan-runtime-core`
* **Executor Runtimes (Java)** → `wayang/executors/`
* **Golek Inference Engine** → `inference-golek/`

---

## 🔁 Wayang Control Plane — Sequence Diagram

```mermaid
sequenceDiagram
    participant UI as Wayang UI
    participant GW as Iket Gateway
    participant CP as Wayang Control Plane
    participant GR as Gamelan Runtime

    UI ->> GW: Create/Deploy Workflow
    GW ->> CP: Auth + Route
    CP ->> CP: Validate + Package
    alt validation error
        CP -->> UI: Reject (schema/permission)
    else valid
        CP ->> GR: Deploy/Trigger Workflow
        alt runtime error/timeout
            GR -->> CP: Failure event
            CP -->> UI: Update Status (failed)
        else success
            GR -->> CP: Event + Status
            CP -->> UI: Update Status
        end
    end
```
