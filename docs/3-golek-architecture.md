
---
# GOLEK INFERENCE SERVER

---

## 🔁 Golek Inference Server — Internal Flowchart

```mermaid
flowchart TD
    A[Receive Inference Request] --> B[Inference API]

    B --> C[Auth & Quota Check]

    C --> D{Authorized?}
    D -- No --> D1[Reject Request]
    D -- Yes --> E[Resolve Model from Registry]
    E --> MR[Model Registry]
    MR --> LMR[Local Model Repository]
    MR --> RMR[Remote Model Repository]

    E --> F[Inference Router]

    F --> G[Request Scheduler]

    G --> H["Build Context (Prompt, Tools, Memory)"]

    H --> I[Check Cache]

    I --> J{Cache Hit?}
    J -- Yes --> J1[Return Cached Result]
    J -- No --> K[Select Runtime]

    K --> L{Local or Cloud?}
    L -- Local --> M[Local Runtime Adapter]
    L -- Cloud --> N[Cloud Runtime Adapter]

    M --> O[Apply LoRA / Adapters]
    N --> O
    N --> P1[External LLM Providers]
    LMR --> M
    M --> LA1[Ollama Runtime]
    M --> LA2["llama.cpp (GGUF)"]
    M --> LA3["LiteRT (.tflite)"]
    M --> LA4[TensorRT]
    M --> LA5[vLLM]
    RMR --> P2["Model Artifact Sources (HF, S3, etc.)"]

    P1 --> O
    O --> P[Run Inference]

    P --> Q{Error?}
    Q -- Yes --> R[Error Handler]

    R --> S{Fallback Allowed?}
    S -- Yes --> F
    S -- No --> T[Return Error]

    Q -- No --> U[Store in Cache]

    U --> V[Emit Metrics & Traces]

    V --> W[Return Result]
```

---

## 🧠 How to read this

Main flow:

```
Request → Auth → Model → Schedule → Context → Runtime → Infer → Cache → Return
```

---

### 🔹 Key decision points

**Authorization**

```
Authorized?
```

**Cache**

```
Cache Hit?
```

**Runtime selection**

```
Local or Cloud?
```

**Fallback**

```
Fallback Allowed?
```

---

### 🔹 Supports your vision

✔ Hybrid local + cloud
✔ Cost-aware routing
✔ LoRA & adapters
✔ Prompt orchestration
✔ Embedding cache
✔ Failover providers
✔ Batching & scheduling
✔ External provider integrations (OpenAI, Gemini, Anthropic, etc.)
✔ Local inference runtimes (Ollama, llama.cpp/gguf, LiteRT/.tflite, TensorRT, vLLM)
✔ Future MCP integration

---

## 🧭 Current Implementation Mapping (Repo)

* **Golek Core / Engine** → `inference-golek/core/`
* **Providers** → `inference-golek/provider/`
* **Adapters** → `inference-golek/adapter/`
* **Runtime** → `inference-golek/runtime/`
* **Repositories** → `inference-golek/repository/`
* **SDK** → `inference-golek/sdk/`

---

## 🧩 Golek SDK Mechanism (Local + Remote)

```mermaid
flowchart TB
    subgraph SDK["Golek SDK"]
        SLocal["Local SDK"]
        SRemote["Remote SDK"]
    end

    subgraph CORE["Golek Core"]
        API["Golek API"]
        Engine["Golek Engine"]
        Runtime["Runtime Adapters"]
    end

    SLocal --> Engine
    SRemote --> API
    API --> Runtime
```

---

## 🔁 Golek Inference — Sequence Diagram

```mermaid
sequenceDiagram
    participant CL as Client
    participant API as Golek API
    participant AUTH as Auth/Quota
    participant REG as Model Registry
    participant REP as Model Repository
    participant CACHE as Cache
    participant RT as Runtime Adapter
    participant LOCAL as Local Runtime
    participant LLM as External LLM
    participant OBS as Metrics/Tracing

    CL ->> API: infer(request)
    API ->> AUTH: verify key + quota
    AUTH -->> API: allowed/denied
    alt denied
        API -->> CL: error (unauthorized/limit)
    else allowed
        API ->> REG: resolve model + policy
        REG ->> REP: fetch model metadata/artifact
        REP -->> REG: model spec
        REG -->> API: model + policy
        API ->> CACHE: lookup(request hash)
        alt cache hit
            CACHE -->> API: cached result
            API -->> CL: response (cached)
        else cache miss
            API ->> RT: route to runtime
            alt local runtime
                RT ->> LOCAL: run inference
                LOCAL -->> RT: result
            else cloud runtime
                RT ->> LLM: inference call
                LLM -->> RT: result
            end
            alt runtime error/timeout
                RT -->> API: error
                API ->> REG: fallback policy?
                alt fallback allowed
                    API ->> RT: route to alternate runtime
                    RT ->> LLM: inference call (fallback)
                    LLM -->> RT: result
                    RT -->> API: result
                else fallback denied
                    API -->> CL: error (failed)
                end
            else success
                RT -->> API: result
                API ->> CACHE: store result
                API ->> OBS: emit metrics/traces
                API -->> CL: response
            end
        end
    end
```
