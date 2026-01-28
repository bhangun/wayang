# Wayang Platform — Core Concepts

This document defines the fundamental concepts of the Wayang Platform.

Wayang is a distributed orchestration and negotiation system built around explicit state, workflows, and executors.

---

## 1. Workflow

A **Workflow** is a directed graph of steps (nodes) representing a business or system process.

Characteristics:
- May be acyclic (DAG) or cyclic
- May be synchronous or asynchronous
- May include human and AI steps
- May include negotiation and approval logic

A workflow defines:
- What should happen
- In what order
- Under what conditions

---

## 2. Node

A **Node** is a single executable step within a workflow.

Types of nodes:
- Task node (execute work)
- Decision node (branching logic)
- Approval node (human decision)
- Agent node (AI execution)
- Event node (wait for event)
- Compensation node (rollback logic)

Each node:
- Has an input and output
- May call an executor
- Emits execution events

---

## 3. Transition

A **Transition** connects two nodes.

It defines:
- The condition under which the next node executes
- The mapping of outputs to inputs
- The control flow semantics

Transitions may be:
- Conditional
- Parallel
- Sequential
- Event-triggered

---

## 4. Execution Token

An **Execution Token** represents a single running instance of a workflow.

It contains:
- Workflow version
- Current node(s)
- Execution context
- State snapshot
- Correlation identifiers

Tokens make workflows:
- Stateful
- Recoverable
- Replayable

---

## 5. Execution Context

The **Execution Context** is the data space shared across nodes.

Includes:
- Inputs
- Intermediate results
- Metadata
- Secrets (by reference)
- Policy and approval state

Context is:
- Immutable per step (versioned)
- Propagated between nodes
- Serializable

---

## 6. Executor

An **Executor** is an entity capable of performing work for a node.

Executors may be:
- Microservices
- Containers
- Serverless functions
- Humans (via UI)
- AI agents
- External systems

Executors:
- Advertise capabilities
- Receive tasks
- Return results
- Send heartbeats

---

## 7. Policy

A **Policy** is a rule governing workflow execution.

Policies may define:
- Who can execute a node
- Preconditions for execution
- Approval requirements
- Resource limits
- Compliance constraints

Policies are evaluated:
- Before execution
- During negotiation
- At state transitions

---

## 8. Approval

An **Approval** is a human decision point embedded in a workflow.

Characteristics:
- May block execution
- May include negotiation
- Is persisted
- Is auditable

Approvals may include:
- Price negotiation
- Scope agreement
- Risk acceptance
- Contract confirmation

---

## 9. Negotiation

**Negotiation** is a multi-step interaction between parties.

Used for:
- Marketplace logic
- SLA agreement
- Pricing and quota
- Resource allocation

Negotiation state is:
- Explicit
- Persisted
- Workflow-driven

---

## 10. Event

An **Event** represents a significant occurrence in the system.

Examples:
- WorkflowStarted
- NodeCompleted
- ApprovalGranted
- ExecutorFailed

Events are:
- Emitted internally
- Consumable externally
- Correlatable to tokens

---

## 11. Plugin

A **Plugin** extends Wayang behavior.

Plugins may provide:
- New node types
- New executor types
- New persistence backends
- New policy engines
- New protocol adapters

Plugins are:
- Contract-based
- Versioned
- Isolated

---

## 12. Agent

An **Agent** is an autonomous or semi-autonomous executor.

Agents:
- May reason over context
- May call tools
- May communicate with other agents
- May involve human override

Agents are first-class participants in workflows.

---

## 13. Control Plane

The **Control Plane** manages intent and configuration.

It handles:
- Workflow registration
- Policy management
- Executor registry
- Approval UI
- Inspection and monitoring

---

## 14. Data Plane

The **Data Plane** executes work.

It includes:
- Workflow engine
- Executors
- Event processors

---

## 15. State Store

The **State Store** persists system state.

Stores:
- Workflow definitions
- Execution tokens
- Approval records
- Negotiation state
- Audit logs

---

## 16. Idempotency

All execution in Wayang is designed to be:
- Repeatable
- Safe to retry
- Deterministic per token

This allows:
- Crash recovery
- Message replay
- At-least-once delivery

---

## 17. Federation (Future)

Federation allows:
- Multiple Wayang clusters
- Cross-domain workflows
- Shared negotiation state
- Inter-platform coordination

---

## Mental Model Summary

Wayang is best understood as:

> A system that persists **intent**, executes **steps**, coordinates **actors**, and enforces **rules**.

Or more simply:

> **Workflow + State + Executors + Policy + Negotiation**

---

## Naming Convention

Key objects:

- Workflow
- Node
- Token
- Context
- Executor
- Policy
- Event
- Plugin
- Agent

These terms are consistent across APIs, storage, and protocols.

---

## Status

This document defines conceptual truth.  
Implementation may evolve, but these concepts remain stable.

---