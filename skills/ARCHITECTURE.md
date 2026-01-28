# Wayang Platform — Architecture

This document describes the high-level architecture of the Wayang Platform.

Wayang is designed as a **distributed control plane and execution fabric** for orchestrating workflows, negotiations, and agents across heterogeneous systems.

---

## 1. Architectural Overview

Wayang consists of three primary layers:

```

+----------------------+
|     Control Plane    |
+----------------------+
|
| gRPC / HTTP / Events
v
+----------------------+
|    Workflow Engine   |
+----------------------+
|
| Dispatch
v
+----------------------+
|       Executors      |
+----------------------+

```

### Roles:

- **Control Plane**  
  Manages configuration, policies, approvals, and lifecycle.

- **Workflow Engine**  
  Executes workflow semantics and state transitions.

- **Executors**  
  Perform actual work (services, agents, humans, or systems).

---

## 2. Control Plane

### Responsibilities:
- Workflow registration
- Policy and approval management
- Executor registration
- Negotiation state management
- Configuration distribution
- Observability and inspection APIs

### Interfaces:
- REST / gRPC API
- Event subscriptions
- Admin and operator APIs

---

## 3. Workflow Engine

### Responsibilities:
- Interpret workflow definitions
- Maintain execution tokens
- Orchestrate nodes and transitions
- Handle retries, timeouts, and compensation
- Emit execution events

### Core Concepts:
- **Workflow Definition**
- **Execution Token**
- **Node**
- **Transition**
- **Execution Context**

---

## 4. Executors

Executors are **pluggable execution units**.

They may be:
- Microservices
- Containers
- Serverless functions
- AI agents
- Human-in-the-loop interfaces
- External systems

### Executor Capabilities:
- Task execution
- Result reporting
- Heartbeats
- Capability discovery

---

## 5. Plugin System

Wayang uses a plugin architecture to extend:

- Workflow node types
- Persistence backends
- Policy engines
- Security providers
- Protocol adapters
- Executor types

Plugins are:
- Versioned
- Isolated
- Hot-loadable (optional)
- Contract-driven

---

## 6. Event-Driven Core

Wayang is fundamentally **event-driven**.

Key events:
- WorkflowStarted
- NodeExecuted
- ApprovalRequested
- ApprovalGranted
- ExecutionFailed
- WorkflowCompleted

Event channels:
- Kafka / NATS / AMQP
- Webhooks
- Internal event bus

---

## 7. State & Persistence

State is treated as a **first-class artifact**.

Persisted objects:
- Workflow definitions
- Execution tokens
- Negotiation state
- Approval state
- Audit logs

Backends:
- SQL
- NoSQL
- Object storage

State transitions are:
- Versioned
- Auditable
- Replayable

---

## 8. Security Architecture

Security is enforced at multiple layers:

- API authentication (OIDC/JWT)
- Policy-based authorization
- Workflow-level ACL
- Executor trust model
- Secret injection

---

## 9. Multi-Tenancy Model

Wayang supports multi-tenancy by:

- Namespace isolation
- Tenant-level policies
- Resource quotas
- Executor scoping
- Separate state domains

---

## 10. Communication Patterns

Supported patterns:

- Request/Response (gRPC, HTTP)
- Event-based (pub/sub)
- Stream processing
- Long-running task callbacks

---

## 11. Failure Model

Wayang assumes **partial failure**:

- Executors may disappear
- Messages may be delayed
- Networks may partition

Handled by:
- Idempotent execution
- Retry strategies
- Dead-letter queues
- Compensation workflows
- Manual recovery hooks

---

## 12. AI & Agent Integration

AI agents are treated as:

- Executors
- Workflow nodes
- Policy evaluators
- Negotiation participants

Agent features:
- Tool access
- Memory/context passing
- Agent-to-agent messaging
- Human override

---

## 13. Deployment Topologies

Supported deployments:

- Single-node
- Clustered
- Edge + cloud hybrid
- On-premise
- Federated multi-cluster

---

## 14. Design Principles

- Protocol-agnostic
- Execution-environment neutral
- Plugin-first
- Event-driven
- State-explicit
- Human + AI cooperative

---

## Summary

Wayang is not a monolithic workflow engine.

It is a **distributed orchestration fabric** capable of:
- Coordinating services
- Managing negotiations
- Orchestrating agents
- Enforcing policy
- Persisting intent and state

Wayang is designed to operate as:
- A workflow engine
- A control plane
- A marketplace coordinator
- An AI agent fabric

All at once.