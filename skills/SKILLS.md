# Wayang Platform — Skills

This document describes the core **skills (capabilities)** of the Wayang Platform.  
Skills represent reusable, composable abilities that can be executed by workflows, agents, or external systems.

Wayang is designed as a **control plane + workflow engine + executor network**, where each skill can be implemented by:
- Built-in engines
- External executors
- Plugins
- AI agents
- Third-party systems

---

## 1. Workflow Orchestration

**Skill:** `workflow.orchestrate`

Capabilities:
- Define DAG and cyclic workflows
- Execute step-by-step or event-driven flows
- Support synchronous and asynchronous nodes
- Handle retries, timeouts, and compensation logic
- Token-based execution (stateful workflows)

---

## 2. Policy & Approval Management

**Skill:** `policy.evaluate`

Capabilities:
- Human-in-the-loop approval flows
- Multi-stage negotiation (price, terms, quota, rules)
- Policy enforcement before execution
- Approval state persistence
- Pluggable rule engines (OPA, custom logic)

---

## 3. Executor Dispatching

**Skill:** `executor.dispatch`

Capabilities:
- Route tasks to executors via:
  - gRPC
  - HTTP
  - Message broker (Kafka, NATS, AMQP)
- Support heterogeneous executors:
  - Microservices
  - Containers
  - Serverless
  - AI agents
- Dynamic executor discovery

---

## 4. Event Processing

**Skill:** `event.process`

Capabilities:
- Emit workflow lifecycle events
- Consume external system events
- Correlate events with workflow tokens
- Support pub/sub and stream processing
- Enable event-driven workflows

---

## 5. State & Persistence

**Skill:** `state.manage`

Capabilities:
- Persist workflow execution state
- Persist approval and negotiation state
- Support multiple backends:
  - SQL
  - NoSQL
  - Object storage
- Versioned state transitions
- Auditable execution history

---

## 6. Integration & Interoperability

**Skill:** `integration.connect`

Capabilities:
- REST integration
- gRPC integration
- Webhook handling
- Message queue integration
- Adapter-based protocol bridging

---

## 7. Plugin System

**Skill:** `plugin.extend`

Capabilities:
- Runtime plugin loading
- Plugin lifecycle management
- Plugin isolation
- Extension points for:
  - Executors
  - Policies
  - Workflow nodes
  - Persistence
  - Security
- Versioned plugin contracts

---

## 8. Security & Identity

**Skill:** `security.enforce`

Capabilities:
- Authentication (OIDC, JWT, API keys)
- Authorization (RBAC, ABAC)
- Workflow-level access control
- Executor trust validation
- Secret management integration

---

## 9. Observability

**Skill:** `observe.monitor`

Capabilities:
- Distributed tracing
- Structured logging
- Metrics export (Prometheus/OpenTelemetry)
- Workflow visualization
- Execution replay

---

## 10. Negotiation & Marketplace Logic

**Skill:** `negotiation.coordinate`

Capabilities:
- Price negotiation flows
- Quota and capacity agreement
- SLA definition
- Multi-party interaction
- Contract state management

---

## 11. AI & Agent Orchestration

**Skill:** `agent.orchestrate`

Capabilities:
- Invoke AI agents as workflow nodes
- Agent-to-agent communication
- Tool execution via agents
- Memory/context propagation
- Hybrid human + AI workflows

---

## 12. Configuration & Compilation

**Skill:** `config.compile`

Capabilities:
- Validate workflow definitions
- Compile high-level workflow DSL into executable plans
- Versioned configuration
- Environment-specific overlays
- Safe rollout of changes

---

## 13. Failure Handling & Recovery

**Skill:** `failure.recover`

Capabilities:
- Automatic retries
- Dead-letter routing
- Compensation workflows
- Partial rollback
- Manual recovery hooks

---

## 14. Multi-Tenancy

**Skill:** `tenant.isolate`

Capabilities:
- Namespace isolation
- Resource quotas
- Tenant-level policies
- Tenant-specific executors
- Billing and usage tracking

---

## 15. Control Plane APIs

**Skill:** `control.manage`

Capabilities:
- Workflow lifecycle management
- Policy management
- Executor registration
- State inspection
- Admin and operator APIs

---

## Design Philosophy

Wayang treats **skills as first-class concepts**:

- Skills are composable
- Skills are replaceable
- Skills are protocol-agnostic
- Skills are execution-environment neutral

This enables Wayang to act as:
- A workflow engine
- A negotiation engine
- An orchestration platform
- A marketplace control plane
- An AI agent coordinator

---

## Future Skills (Planned)

- `simulation.run` — Dry-run and what-if analysis
- `optimizer.route` — Cost and latency optimization
- `compliance.audit` — Regulatory enforcement
- `federation.bridge` — Multi-Wayang interop
- `economy.settle` — Billing and settlement logic

---

## Skill Naming Convention

```

<domain>.<verb>

```

Examples:
- `workflow.orchestrate`
- `policy.evaluate`
- `executor.dispatch`
- `agent.orchestrate`

---

