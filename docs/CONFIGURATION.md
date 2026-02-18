# Wayang Configuration Reference

This document summarizes runtime configuration flags for Wayang core subsystems.

---

## Gamelan (Workflow Engine)

### Workflow Modes

* `mode: FLOW` is default (agentic loops allowed)
* `mode: DAG` is opt‑in (acyclic validation + DAG helpers)
* `mode: STATE` for long‑running, event‑driven workflows

### DAG Module Flags

| Flag | Default | Notes |
| --- | --- | --- |
| `gamelan.dag.plugin.enabled` | `true` | Enables DAG validator plugin when `mode: DAG` |
| `gamelan.dag.scheduler.enabled` | `false` | Enables topological ordering for ready nodes |
| `gamelan.dag.validator.enabled` | `true` | Turns DAG validator on/off |
| `gamelan.dag.validator.allowMultipleRoots` | `false` | Allow multiple DAG roots |
| `gamelan.dag.validator.allowOrphanNodes` | `false` | Allow orphan nodes |
| `gamelan.dag.validator.maxDepth` | `100` | Max DAG depth |
| `gamelan.dag.validator.maxWidth` | `50` | Max DAG width |

---

## Gollek (Inference)

### Local Runtime Selection (Planned)

These flags describe the intended runtime selection strategy for local inference.

| Flag | Default | Notes |
| --- | --- | --- |
| `gollek.local.runtime` | `auto` | `ollama` | `gguf` | `litert` | `tensorrt` | `vllm` |
| `gollek.local.ollama.baseUrl` | `http://localhost:11434` | Ollama API base |
| `gollek.local.gguf.modelPath` | empty | GGUF file path (llama.cpp binding) |
| `gollek.local.litert.modelPath` | empty | `.tflite` model path |
| `gollek.local.tensorrt.enginePath` | empty | TensorRT engine path |
| `gollek.local.vllm.baseUrl` | empty | vLLM server base URL |

---

## Wayang Platform

### Multi-Tenancy

| Flag | Default | Notes |
| --- | --- | --- |
| `wayang.multitenancy.enabled` | `false` | Enabled automatically by `tenant-*-ext` modules |

Enable per component by adding:
* `tenant-gollek-ext`
* `tenant-gamelan-ext`
* `tenant-wayang-ext`

See `wayang-enterprise/modules/tenant/README.md` for details.
