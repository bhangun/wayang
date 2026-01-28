# Wayang Platform — Executors

This document defines the executor model of the Wayang Platform.

An **Executor** is any entity capable of performing work on behalf of a workflow node.

Executors form the **execution fabric** of Wayang.

---

## 1. Definition

An executor is responsible for:
- Receiving tasks
- Executing them
- Reporting results
- Emitting status and heartbeats

Executors do NOT manage workflows.  
They only execute tasks assigned by the Workflow Engine.

---

## 2. Executor Types

Executors may be implemented as:

- Microservices
- Containers
- Serverless functions
- Batch jobs
- Human interfaces (approval UI)
- AI agents
- External systems (via adapters)

All executor types follow the same logical contract.

---

## 3. Executor Capabilities

Each executor declares its capabilities:

Examples:
- `http.call`
- `db.query`
- `payment.charge`
- `agent.reason`
- `approval.request`

Capabilities are used for:
- Routing
- Scheduling
- Policy enforcement
- Negotiation

---

## 4. Executor Lifecycle

1. **Registration**
   - Executor registers with the Control Plane
   - Provides:
     - ID
     - Capabilities
     - Protocol
     - Metadata

2. **Discovery**
   - Workflow Engine selects executor based on:
     - Capability match
     - Policy
     - Load
     - Availability

3. **Dispatch**
   - Task is sent to executor
   - Includes:
     - Execution token
     - Node definition
     - Context snapshot

4. **Execution**
   - Executor performs work
   - May emit progress events

5. **Result**
   - Executor returns:
     - Success result
     - Failure reason
     - Partial output

6. **Heartbeat**
   - Executor periodically reports liveness

---

## 5. Executor Contract (Logical)

An executor must support:

- `execute(task)`
- `status(taskId)`
- `cancel(taskId)`
- `capabilities()`
- `heartbeat()`

Transport is pluggable:
- gRPC
- HTTP
- Message queue
- Event stream

---

## 6. Task Model

A task contains:

- Task ID
- Execution token
- Node ID
- Input context
- Policy constraints
- Timeout and retry hints

Tasks are:
- Immutable
- Idempotent
- Traceable

---

## 7. Result Model

Executor returns:

- Status: SUCCESS | FAILED | PENDING
- Output payload
- Error (if any)
- Metrics
- Logs (optional)

Results are correlated to:
- Task ID
- Token ID
- Node ID

---

## 8. Executor Isolation

Executors may run:
- In different clusters
- In different networks
- Under different trust domains

Wayang enforces:
- Authentication
- Authorization
- Capability scoping
- Token validation

---

## 9. Human Executors

Humans are treated as executors via:
- Approval UI
- Task inbox
- Negotiation interface

Human executors:
- Receive tasks
- Provide decisions
- Trigger transitions

---

## 10. Agent Executors

AI agents act as executors that:
- Reason over context
- Call tools
- Communicate with other agents
- Produce structured outputs

Agents may be:
- Stateless
- Stateful
- Multi-step

---

## 11. Failure Handling

Executor failures include:
- Timeout
- Crash
- Network loss
- Invalid result

Handled by:
- Retry policies
- Executor reassignment
- Compensation nodes
- Dead-letter routing

---

## 12. Security Model

Executors must:
- Authenticate to Wayang
- Be authorized per capability
- Validate task signatures
- Protect secrets

Secrets are:
- Passed by reference
- Injected securely
- Never persisted in plain form

---

## 13. Observability

Executors emit:
- Execution logs
- Progress events
- Metrics
- Traces

Used for:
- Debugging
- Replay
- Optimization
- Billing

---

## 14. Scaling Model

Executors scale independently of:
- Control Plane
- Workflow Engine

Scaling strategies:
- Horizontal scaling
- Queue-based backpressure
- Capability sharding

---

## 15. Design Principles

- Executors are stateless where possible
- Executors do not own workflow logic
- Executors are replaceable
- Executors are capability-driven
- Executors are protocol-agnostic

---

## Summary

Executors are the workers of Wayang.

They:
- Execute nodes
- Produce results
- Emit signals

Wayang:
- Coordinates them
- Routes tasks
- Persists state
- Enforces rules

This separation allows Wayang to scale, federate, and integrate across environments.

---