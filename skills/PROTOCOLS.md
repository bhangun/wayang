# Wayang Platform — Protocols

This document defines the communication protocols used by the Wayang Platform.

Wayang is designed to be **protocol-agnostic**.  
All interactions are abstracted behind contracts and adapters.

---

## 1. Protocol Philosophy

Wayang separates:
- **Semantics** (what a message means)
- **Transport** (how it is delivered)

This allows:
- Multiple transports
- Pluggable adapters
- Future protocol support

---

## 2. Core Communication Paths

Main communication flows:

1. Control Plane ↔ Workflow Engine  
2. Workflow Engine ↔ Executors  
3. Wayang ↔ External Systems  
4. Wayang ↔ Agents  
5. Wayang ↔ Event Brokers  

Each path may use different protocols.

---

## 3. Supported Protocol Types

Wayang supports the following protocol families:

- **Request/Response**
  - HTTP/REST
  - gRPC

- **Event-Based**
  - Kafka
  - NATS
  - AMQP
  - Cloud Pub/Sub

- **Streaming**
  - gRPC streaming
  - WebSocket

- **Callback-Based**
  - Webhooks

---

## 4. Control Plane APIs

Control Plane exposes:

- Workflow management
- Policy management
- Executor registry
- Approval endpoints
- State inspection

Typically via:
- REST
- gRPC

All APIs are:
- Versioned
- Authenticated
- Audited

---

## 5. Executor Protocol

Executors communicate with the Workflow Engine using:

Logical operations:
- `execute(task)`
- `status(taskId)`
- `cancel(taskId)`
- `heartbeat()`

These may be transported over:
- gRPC
- HTTP
- Message queues
- Event streams

---

## 6. Event Protocol

Events are first-class messages.

Event properties:
- Type
- Timestamp
- Token ID
- Node ID
- Payload

Events may be:
- Published to brokers
- Pushed via webhooks
- Streamed to consumers

---

## 7. Approval & Human Interaction

Human interaction uses:

- Web UI (HTTP)
- Webhooks
- Notification channels (email, chat, mobile)

Approval results are returned via:
- REST
- Event callbacks

---

## 8. Agent Protocol

Agents interact via:

- Executor protocol
- Tool invocation protocol
- Agent-to-agent messaging

Agent communication supports:
- Context passing
- Tool calls
- Delegation
- Result streaming

---

## 9. Protocol Adapters

Protocol adapters translate between:

- Wayang internal model
- External wire formats

Examples:
- REST adapter
- gRPC adapter
- Kafka adapter
- Webhook adapter

Adapters are implemented as plugins.

---

## 10. Message Correlation

All protocol messages include:

- Correlation ID
- Token ID
- Node ID
- Trace ID

This enables:
- Distributed tracing
- Replay
- Debugging

---

## 11. Reliability Guarantees

Wayang supports:

- At-least-once delivery
- Idempotent handlers
- Retry semantics
- Dead-letter routing

Exact guarantees depend on:
- Transport
- Configuration
- Executor behavior

---

## 12. Security in Protocols

All protocols support:

- Authentication
- Authorization
- Encryption (TLS)
- Message signing (optional)

Secrets are:
- Never embedded in payloads
- Resolved at execution time

---

## 13. Versioning Strategy

Protocols are versioned by:

- URL path (REST)
- Service version (gRPC)
- Message schema version (events)

Backward compatibility is preferred.

---

## 14. Extending Protocols

New protocols can be added by:

- Implementing adapter plugin
- Declaring supported operations
- Registering with engine

No core changes required.

---

## Summary

Wayang does not bind itself to a single protocol.

Instead, it provides:
- A semantic core
- Pluggable transports
- Unified execution model

This allows Wayang to integrate with:
- Modern microservices
- Legacy systems
- Event-driven platforms
- AI agents
- Human interfaces

All using the same workflow and state model.

---