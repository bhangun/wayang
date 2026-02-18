
---
# GOLEK INFERENCE SERVER

---

## Multi-Tenancy Activation

Multi-tenancy is disabled by default and enabled per component via extensions.

* Gollek: `tenant-gollek-ext`
* Gamelan: `tenant-gamelan-ext`
* Wayang: `tenant-wayang-ext`

The extensions automatically set `wayang.multitenancy.enabled=true`. See `wayang-enterprise/modules/tenant/README.md` for details.

## 🔁 Gollek Inference Server — Internal Flowchart

```mermaid
flowchart TD
  A[Receive Inference Request]
  B[Inference API]
  C[Auth & Quota Check]
  D{Authorized?}
  D1[Reject Request]

  E[Resolve Model & Provider]
  MR[Model Registry]
  CRPR[Cloud/Remote Provider Registry]
  MM[Model & Provider Metadata]
  CMX[Capability Matrix]

  H["Build Context (Prompt, Tools, Memory)"]

  G[Request Scheduler]
  I[Check Cache]
  J{Cache Hit?}
  J1[Return Cached Result]

  K[Select Runtime]
  L{Local or Cloud?}

  LMR[Local Model Repository]
  RMR[Remote Model Repository]
  LA[Local Artifact Exists?]
  P2["Model Artifact Sources (HF, S3, etc.)"]
  DL[Download/Sync Artifacts]

  M[Local/Standalone Adapter]
  N[Cloud/Remote Runtime Adapter]
  CAP{Adapters Supported?}
  O[Apply LoRA / Adapters]

  VAR{Select Artifact Variant}
  IQ{Quantized Variant?}
  RC{Runtime Supports Format?}
  IIE[Internal Inference Engine Runtime]
  LAP[Local Inference Engine Adapter]
  LMDL[Load Local Model]
  FMT{Which Format?}
  LA1["GGUF (llama.cpp)"]
  LA2[".tflite (LiteRT)"]
  LA5[vLLM]

  P1[Other External LLM Providers]
  GG["Gemini"]
  OAI["OpenAI"]
  OLL[Ollama]

  P[Run Inference]
  SR{Streaming Response?}
  ST[Stream Tokens]
  Q{Error?}
  R[Error Handler]
  S{Fallback Allowed?}
  T[Return Error]

  U[Store in Cache]
  V[Emit Metrics & Traces]
  W[Return Result]

  subgraph Community["Community / Individual Mode"]
    CA[Community Mode]
  end

  subgraph Enterprise["Enterprise Mode (Advanced Context)"]
    FFC[Feature Flags / Tenant Config]
    EM{Enterprise Mode?}
    PG[Policy / Guardrails]
    RL[Multi-tenant Rate Limits]
    AUD[Audit / Compliance Hooks]
  end

  A --> B --> C --> D
  D -- No --> D1
  D -- Yes --> FFC --> EM

  EM -- No --> CA --> E
  EM -- Yes --> PG --> RL --> AUD --> E

  E --> MR --> MM
  E --> CRPR --> MM
  MM --> CMX
  MM --> H --> G

  G --> I --> J
  J -- Yes --> J1 --> V --> W
  J -- No --> K

  K --> L
  L -- Local --> LMR --> LA
  L -- Cloud --> N

  MR --> LMR

  LA -- No --> RMR --> P2 --> DL --> REG_MODEL[Register Model in Repository]
  REG_MODEL --> M
  LA -- Yes --> M

  M --> VAR
  VAR --> IQ
  IQ -- Yes --> RC
  IQ -- No --> LAP --> LA5

  CMX --> RC
  CMX --> CAP

  RC -- Yes --> IIE --> LMDL --> FMT
  FMT --> LA1
  FMT --> LA2
  RC -- No --> LAP --> LA5

  N --> CRPR
  CRPR --> P1
  CRPR --> GG
  CRPR --> OAI
  CRPR --> OLL

  P1 --> CAP
  GG --> CAP
  OAI --> CAP
  OLL --> CAP
  CAP -- Yes --> O --> P
  CAP -- No --> P

  P --> SR
  SR -- Yes --> ST --> Q
  SR -- No --> Q

  Q -- Yes --> R --> S
  S -- Yes --> E
  S -- No --> T --> V --> W

  Q -- No --> U --> V --> W

  classDef enterprise fill:#1f3b2c,stroke:#2f5a41,stroke-width:2px,color:#e9f7ef;
  class FFC,EM,PG,RL,AUD enterprise;

  classDef community fill:#1f2f4a,stroke:#2f4f7a,stroke-width:2px,color:#e9f0ff;
  class CA community;

```
---

## � Model Download & Persistence Flow

### Download Process

When a model is requested but not found locally, Gollek automatically downloads it from configured sources (Hugging Face, S3, etc.) and registers it in the local repository for future use.

```mermaid
flowchart TD
    A[Model Request] --> B{Model in Repository?}
    B -- Yes --> C[Load from Cache]
    B -- No --> D{Offline Mode?}
    D -- Yes --> E{GGUF Variant Exists?}
    E -- Yes --> F[Use GGUF Variant]
    E -- No --> G[Error: Model Not Found]
    D -- No --> H[Download from Source]
    H --> I{File Already Exists?}
    I -- Yes, Same Size --> J[Skip Download]
    I -- No --> K[Download with Progress]
    K --> L{Ctrl+C Pressed?}
    L -- Yes --> M[Clean Exit, Remove .part File]
    L -- No --> N[Download Complete]
    J --> O[Register in Repository]
    N --> O
    O --> P[Save ModelManifest]
    P --> Q[Print Model Path]
    Q --> C
    F --> C
    C --> R[Ready for Inference]
```

### Key Features

**Model Persistence**
- Downloaded models are registered with `LocalModelRepository`
- `ModelManifest` created with artifact location and metadata
- No re-downloads on subsequent runs

**Smart Download Skipping**
- Checks file existence and size before downloading
- Skips download if file matches expected size

**Robust Cancellation**
- Ctrl+C immediately terminates download (exit code 130)
- Partial `.part` files are cleaned up automatically
- Shutdown hook ensures clean exit

**User Feedback**
- Download progress bar with percentage
- Model save location printed with ✓ checkmark
- Clear error messages for offline mode failures

**Custom Model Paths**
- `--model-path` flag to use custom model files
- Bypasses repository lookup entirely
- Validates file existence before use

### CLI Usage Examples

**First Download**:
```bash
$ gollek run --model Qwen/Qwen2.5-0.5B-Instruct-GGUF --prompt "Hello"
Checking model: Qwen/Qwen2.5-0.5B-Instruct-GGUF... not found
Downloading from Hugging Face...
[========================================] 100%

✓ Model saved to: ~/.gollek/models/gguf/Qwen_Qwen2.5-0.5B-Instruct-GGUF
```

**Subsequent Runs** (no re-download):
```bash
$ gollek run --model Qwen/Qwen2.5-0.5B-Instruct-GGUF --prompt "Hello"
Checking model: Qwen/Qwen2.5-0.5B-Instruct-GGUF... found
Model path: ~/.gollek/models/gguf/Qwen_Qwen2.5-0.5B-Instruct-GGUF
```

**Custom Model Path**:
```bash
$ gollek run --model-path /my/models/custom.gguf --prompt "Hello"
Using model from: /my/models/custom.gguf
```

**Offline Mode**:
```bash
$ gollek run --offline --model Qwen/Qwen2.5-0.5B-Instruct-GGUF --prompt "Hello"
Checking model: Qwen/Qwen2.5-0.5B-Instruct-GGUF... found
# Uses local model, no download attempt
```

---

## �🔧 Local Runtime Focus 

### LiteRT (.tflite)
* **Role**: Mobile/edge‑friendly inference for .tflite models.
* **Path**: Request → Local Runtime Adapter → LiteRT → Model → Result
* **Strengths**: Small footprint, hardware acceleration via delegates.
* **Considerations**: Quantization validation, delegate compatibility, device‑specific constraints.

### GGUF / llama.cpp
* **Role**: Native local inference for GGUF/ggml models (CPU/CUDA).
* **Path**: Request → Local Runtime Adapter → llama.cpp → Model → Result
* **Strengths**: High performance, local control, GGUF support, automatic model conversion.
* **Considerations**: Context isolation, KV cache safety, ABI pinning.
* **Model Formats**: Supports GGUF natively, can convert from PyTorch, SafeTensors, TensorFlow, Flax.
* **Download**: Automatic download from Hugging Face with progress tracking and persistence.

### Ollama (Local Model Runtime)
* **Role**: Local model serving with GGUF/ggml support, optimized for developer machines and edge.
* **Path**: Request → Local Runtime Adapter → Ollama → Model → Result
* **Strengths**: Simple ops, fast local iteration, consistent CLI + server.
* **Considerations**: Model cache location, per‑model concurrency limits, streaming SSE support.



### Local Runtime Comparison (Quick Guide)
| Runtime | Best For | Strengths | Constraints |
|---|---|---|---|
| GGUF (llama.cpp) | Custom native control | High performance, GGUF support | ABI drift, context isolation |
| TFLite (LiteRT) | Mobile/edge | Small footprint, delegate acceleration | Delegate compatibility, quantization |
| Ollama | Fast local iteration | Simple ops, streaming, model caching | Needs local daemon, per‑model limits |

### Local Runtime Decision Guide
```mermaid
flowchart TD
    A[Need local inference?] --> B{Target environment}
    B -- Dev / Laptop --> C[Ollama]
    B -- Edge / Mobile --> D["LiteRT (.tflite)"]
    B -- Server / Custom Native --> E[GGUF / llama.cpp]
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

- ✔ Hybrid local + cloud
- ✔ Cost-aware routing
- ✔ LoRA & adapters
- ✔ Prompt orchestration
- ✔ Embedding cache
- ✔ Failover providers
- ✔ Batching & scheduling
- ✔ External provider integrations (OpenAI, Gemini, Anthropic, etc.)
- ✔ Local inference runtimes (Ollama, llama.cpp/gguf, LiteRT/.tflite, TensorRT, vLLM)
- ✔ Automatic model download & persistence (Hugging Face, S3)
- ✔ Model format conversion (PyTorch/SafeTensors → GGUF)
- ✔ Smart download skipping & cancellation
- ✔ Custom model path support
- ✔ Offline mode with GGUF variant fallback
- ✔ Future MCP integration

---

## 🧭 Current Implementation Mapping (Repo)

* **Gollek Core / Engine** → `inference-gollek/core/`
  - `gollek-spi` - Service Provider Interface with `ModelFormat` enum
  - `gollek-engine` - Core inference orchestration
  - `gollek-provider-core` - Provider abstraction layer
* **Providers** → `inference-gollek/provider/`
  - `gollek-ext-cloud-ollama` - Ollama integration
  - `gollek-ext-cloud-gemini` - Google Gemini integration
  - `gollek-ext-cloud-cerebras` - Cerebras integration
* **Adapters** → `inference-gollek/inference/format/`
  - `gollek-ext-format-gguf` - GGUF/llama.cpp adapter
  - `gollek-gguf-converter` - Model format conversion service
* **Runtime** → `inference-gollek/runtime/`
* **Repositories** → `inference-gollek/repository/`
  - `gollek-model-repo-local` - Local model storage & manifest management
  - `gollek-model-repo-hf` - Hugging Face download client with progress tracking
* **SDK** → `inference-gollek/sdk/`
  - `gollek-sdk-java-local` - Local SDK with model registration
  - `gollek-sdk-core` - Core SDK interfaces
* **CLI** → `inference-gollek/ui/gollek-cli`
  - Command-line interface with `--model-path` and `--offline` flags

---

## 🧩 Gollek SDK Mechanism (Local + Remote)

```mermaid
flowchart TB
    subgraph SDK["Gollek SDK"]
        SLocal["Local SDK"]
        SRemote["Remote SDK"]
    end

    subgraph CORE["Gollek Core"]
        API["Gollek API"]
        Engine["Gollek Engine"]
        Runtime["Runtime Adapters"]
    end

    SLocal --> Engine
    SRemote --> API
    API --> Runtime
```

---

## 🔁 Gollek Inference — Sequence Diagram

```mermaid
sequenceDiagram
    participant CL as Client
    participant API as Gollek API
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

---

## ✅ Step-By-Step Build Order (Minimal → Production)

1. **Boot Gollek Core**  
   Focus: inference pipeline orchestration.  
   Code: `inference-gollek/core/`

2. **Add Model Registry + Repositories**  
   Focus: model metadata + artifact resolution.  
   Code: `inference-gollek/core/gollek-model-repo-core`, `inference-gollek/repository/`

3. **Enable Provider SPI**  
   Focus: pluggable inference backends.  
   Code: `inference-gollek/core/gollek-provider-core`, `inference-gollek/provider/`

4. **Attach Runtime Adapters**  
   Focus: local vs cloud runtime selection.  
   Code: `inference-gollek/runtime/`, `inference-gollek/adapter/`

5. **Add SDKs**  
   Focus: local/remote developer integration.  
   Code: `inference-gollek/sdk/`

---

## ✅ Production Readiness Checklist (Gollek)

* Auth/quota enforcement per tenant
* Provider fallback policy configured
* Model registry cache warmed
* Local runtimes validated (Ollama, llama.cpp, LiteRT, TensorRT, vLLM)
* External provider rate limits handled
* Prompt/context redaction + safety checks
* Metrics + traces exported (OpenTelemetry)
