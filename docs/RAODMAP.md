# Wayang Platform Roadmap

This roadmap is organized by subsystem and prioritizes production readiness.

## Wayang (Control Plane)

### P0
1. Control plane hardening (schema validation + RBAC)  
   Deliverable: strict schema enforcement on all workflow/agent definitions, tenant-scoped RBAC policies, and audit logs for denied operations.
2. Secrets lifecycle integration (Vault/KMS + audit)  
   Deliverable: unified secret store, rotation hooks, access audit trail, and per-tenant secret namespaces.
3. Plugin registry validation + signing policy  
   Deliverable: signature verification on plugin upload, schema compatibility checks, and rejection reason reporting.

### P1
1. Deployment manager improvements (blue/green, rollback)  
   Deliverable: versioned rollout strategies, health-based promotion, and automated rollback with audit.
2. Runtime registry health + capacity awareness  
   Deliverable: runtime health scoring, capacity reporting, and placement decisions that respect capacity.
3. Tenant quota enforcement (control plane)  
   Deliverable: quota policies for workflow runs, storage, and tool usage with clear error messaging.

### P2
1. Governance workflows (approvals, review gates)  
   Deliverable: approval rules on deployments, change reviews, and policy exceptions workflow.
2. Audit query API + export  
   Deliverable: queryable audit logs with filters, export to storage, and retention controls.
3. Admin UI workflows for policy ops  
   Deliverable: UI flows to manage RBAC, quotas, plugin policies, and audit reports.

---

## Gamelan (Execution Plane)

### P0
1. Service discovery (optional module)  
   Deliverable: discovery adapter interface, Consul plugin, and static fallback resolver.
2. Executor capacity + backpressure  
   Deliverable: executor slot reporting, dispatch throttling, and per-queue backpressure handling.
3. Idempotency + exactly-once semantics  
   Deliverable: token-scoped idempotency keys, result de-duplication, and replay-safe callbacks.

### P1
1. DLQ + replay tooling  
   Deliverable: DLQ persistence, replay CLI, and retry metadata tracking.
2. SLA-aware scheduling (priority, latency budgets)  
   Deliverable: SLA tags, priority queues, and deadline-aware dispatch selection.
3. Multi-tenant isolation (fair-share dispatch)  
   Deliverable: tenant queues, fair-share selection, and per-tenant limits.

### P2
1. Audit/execution trace API  
   Deliverable: per-run trace queries, node-level timelines, and exportable traces.
2. Executor fleet intelligence (health scoring)  
   Deliverable: health scoring beyond heartbeat, drift detection, and alerting hooks.
3. Auto-scaling hooks  
   Deliverable: metrics-based scale signals and integration points for infrastructure autoscalers.

---

## Gollek (Inference Plane)

### P0
1. Provider fallback policy (per-tenant)  
   Deliverable: per-tenant fallback rules, priority order, and error classification.
2. Model registry cache warming  
   Deliverable: prefetch popular models, cold-start detection, and cache eviction rules.
3. Local runtime hardening (Ollama, llama.cpp, LiteRT, TensorRT, vLLM)  
   Deliverable: validated adapters, standardized metrics, and health checks per runtime.
4. SDK local wiring (CDI-safe) + timeout enforcement  
   Deliverable: CDI-aware local SDK creation, engine timeouts enforced, and retry classification.

### P1
1. Cost-aware routing (local vs cloud)  
   Deliverable: cost model inputs, configurable routing thresholds, and per-tenant controls.
2. Batch scheduling + KV cache  
   Deliverable: request batching, KV cache integration, and latency/throughput tradeoff config.
3. Safety & redaction pipeline  
   Deliverable: PII redaction, prompt filtering, and policy enforcement hooks.
4. Routing cache + model cache correctness  
   Deliverable: bounded routing decision cache, model cache invalidation on updates.
5. SDK streaming robustness  
   Deliverable: non-blocking streaming client, keep-alive handling, and backpressure.

### P2
1. Model provenance + lineage  
   Deliverable: model metadata lineage, version pinning, and audit history.
2. Multi-provider A/B routing  
   Deliverable: traffic splitting, experiment tracking, and result evaluation hooks.
3. Low-latency streaming inference  
   Deliverable: streaming APIs, partial results, and backpressure handling.
4. Unified exception taxonomy  
   Deliverable: one error model across engine/provider/sdk with stable error codes.
