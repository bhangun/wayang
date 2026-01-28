# Wayang Platform — Workflows

This document defines how workflows are represented and executed in the Wayang Platform.

A workflow describes **what should happen**, **in what order**, and **under what conditions**.

Wayang workflows are:
- Explicit
- Stateful
- Event-driven
- Extensible
- Human and AI friendly

---

## 1. Workflow Definition

A **Workflow** is a directed graph of nodes connected by transitions.

A workflow defines:
- Nodes (steps)
- Transitions (control flow)
- Policies
- Inputs and outputs
- Versioned behavior

Conceptually:

```

Workflow = Nodes + Transitions + Policies + Metadata

````

---

## 2. Workflow Properties

Each workflow has:

- `id` – unique identifier
- `version` – immutable version number
- `name` – human-readable name
- `description` – optional
- `inputs` – expected input schema
- `outputs` – result schema
- `policies` – execution constraints

---

## 3. Node Types

Wayang supports multiple node types:

### 3.1 Task Node
Executes work via an executor.

Example use:
- Call API
- Run job
- Trigger service

---

### 3.2 Decision Node
Chooses a transition based on context.

Example:
- if/else branching
- rule evaluation

---

### 3.3 Approval Node
Pauses execution for human decision.

Example:
- Manager approval
- Contract signing
- Risk acceptance

---

### 3.4 Agent Node
Delegates execution to an AI agent.

Example:
- Reasoning step
- Planning step
- Tool orchestration

---

### 3.5 Event Node
Waits for an external event.

Example:
- Payment received
- Webhook callback
- Message arrival

---

### 3.6 Compensation Node
Handles rollback or correction.

Example:
- Refund payment
- Undo resource allocation

---

## 4. Transitions

A **Transition** connects nodes.

Transitions define:
- Next node
- Condition (optional)
- Mapping of context data

Transition types:
- Sequential
- Conditional
- Parallel
- Event-triggered

---

## 5. Execution Token

A workflow execution creates an **Execution Token**.

The token tracks:
- Current node(s)
- Execution context
- State
- History
- Correlation IDs

Tokens make workflows:
- Stateful
- Recoverable
- Auditable

---

## 6. Execution Context

The **Execution Context** is the shared data of a workflow.

Contains:
- Input data
- Node outputs
- Metadata
- Policy state
- Approval state

Context is:
- Immutable per step
- Versioned
- Serializable

---

## 7. Example Workflow (Conceptual)

```yaml
id: order-fulfillment
version: 1.0.0

nodes:
  - id: validate-order
    type: task
    capability: order.validate

  - id: charge-payment
    type: task
    capability: payment.charge

  - id: approval-step
    type: approval
    role: finance

  - id: ship-order
    type: task
    capability: logistics.ship

transitions:
  - from: validate-order
    to: charge-payment

  - from: charge-payment
    to: approval-step
    when: context.amount > 1000

  - from: approval-step
    to: ship-order
````

---

## 8. Approval Workflows

Approval nodes:

* Pause execution
* Create approval records
* Notify humans
* Resume when decision is made

Possible decisions:

* Approve
* Reject
* Counter-propose

Approval state is persisted.

---

## 9. Negotiation Workflows

Negotiation is modeled as a workflow:

* Proposal node
* Counter node
* Decision node
* Agreement node

Negotiation state:

* Is explicit
* Is persisted
* Drives transitions

---

## 10. Event-Driven Workflows

Workflows may wait for events:

* External system events
* User actions
* Timer events
* Agent messages

Event nodes:

* Subscribe to events
* Correlate by token ID
* Resume execution

---

## 11. Parallelism

Wayang supports:

* Parallel branches
* Fan-out / fan-in
* Join nodes
* Barrier synchronization

This enables:

* Concurrent execution
* Independent failure handling

---

## 12. Failure Handling

Workflows define:

* Retry policies
* Timeout policies
* Compensation steps
* Dead-letter paths

Failures may:

* Retry same node
* Switch executor
* Trigger compensation
* Abort workflow

---

## 13. Versioning

Workflows are:

* Immutable once published
* Versioned explicitly
* Executed per version

Old tokens continue using:

* Original version

New executions use:

* Latest version

---

## 14. Policies in Workflows

Policies may apply to:

* Whole workflow
* Individual nodes
* Transitions
* Executors

Examples:

* Who can execute
* Who can approve
* Resource limits
* Compliance rules

---

## 15. Human + AI Workflows

Wayang workflows may combine:

* AI agent nodes
* Human approval nodes
* Automated task nodes

Example:

* Agent drafts contract
* Human approves
* Service executes payment

---

## 16. Visualization

Workflows can be visualized as:

* Graphs
* State machines
* Timelines

Visualization is driven from:

* Workflow definition
* Execution history

---

## 17. Design Principles

* Workflows are explicit graphs
* State is always persisted
* Humans and agents are first-class
* Failures are modeled
* Policies are enforced
* Protocols are pluggable

---

## Summary

A Wayang workflow is not just a job.

It is:

* A state machine
* A coordination contract
* A negotiation protocol
* A human-AI process

Wayang workflows define **intent**, not just execution.

---
