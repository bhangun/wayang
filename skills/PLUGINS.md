# Wayang Platform — Plugins

This document defines the plugin system of the Wayang Platform.

Plugins are the primary extension mechanism of Wayang.  
They allow new behavior to be added without modifying the core engine.

---

## 1. Purpose

The plugin system enables:

- Custom workflow node types
- Custom executors
- Custom policy engines
- Custom persistence backends
- Custom protocol adapters
- Custom security providers
- Domain-specific logic

Wayang core remains minimal and stable.  
All variability lives in plugins.

---

## 2. Plugin Definition

A **Plugin** is a packaged extension that implements one or more Wayang extension points.

Each plugin:
- Implements a known contract (SPI)
- Declares metadata
- Is versioned
- Can be enabled or disabled independently

---

## 3. Plugin Types

Common plugin categories:

- **Node Plugins**  
  Provide new workflow node behaviors.

- **Executor Plugins**  
  Provide new executor implementations or adapters.

- **Policy Plugins**  
  Provide custom rule engines or compliance logic.

- **Persistence Plugins**  
  Provide storage backends (SQL, NoSQL, object store).

- **Protocol Plugins**  
  Provide gRPC, HTTP, Kafka, Webhook, or custom protocols.

- **Security Plugins**  
  Provide authentication and authorization providers.

---

## 4. Plugin Metadata

Each plugin declares:

- Plugin ID
- Version
- Provider
- Supported Wayang version
- Capabilities
- Dependencies

Example (conceptual):

```yaml
plugin:
  id: payment-executor
  version: 1.0.0
  provides:
    - executor.payment.charge
  requires:
    - security.jwt
````

---

## 5. Extension Points

Wayang exposes extension points for:

* Workflow node execution
* Executor registration
* Policy evaluation
* State persistence
* Event handling
* Security enforcement

Plugins register against these extension points.

---

## 6. Plugin Lifecycle

1. **Load**

   * Plugin is discovered and loaded
   * Contracts are validated

2. **Initialize**

   * Plugin receives configuration
   * Resources are allocated

3. **Register**

   * Plugin registers capabilities

4. **Execute**

   * Plugin logic is invoked by the engine

5. **Unload**

   * Plugin is stopped and deregistered

---

## 7. Isolation Model

Plugins may be isolated by:

* Classloader
* Process
* Container
* Network boundary

Isolation ensures:

* Fault containment
* Version compatibility
* Security boundaries

---

## 8. Versioning

Plugins follow semantic versioning:

* MAJOR: breaking contract change
* MINOR: new capability
* PATCH: bug fix

Compatibility is checked at load time.

---

## 9. Configuration

Plugins receive configuration via:

* Control Plane
* Environment variables
* Secure secret stores
* Remote configuration

Plugins must be:

* Restart-safe
* Reconfigurable where possible

---

## 10. Plugin Security

Plugins:

* Must declare required permissions
* Are sandboxed where possible
* Cannot access core state directly
* Use controlled APIs

Sensitive plugins (security, billing, identity) may require:

* Signature verification
* Trusted registries

---

## 11. Observability

Plugins integrate with:

* Logging
* Metrics
* Tracing
* Event emission

This ensures:

* Debuggability
* Accountability
* Auditing

---

## 12. Hot Reload (Optional)

Some deployments support:

* Hot plugin loading
* Rolling plugin upgrades
* Dynamic disabling

This is controlled by policy.

---

## 13. Failure Model

If a plugin fails:

* Its tasks fail
* Workflow may retry
* Compensation may run
* Plugin may be quarantined

Core engine remains stable.

---

## 14. Design Principles

* Plugin-first architecture
* Stable contracts
* Strong isolation
* Declarative capabilities
* Minimal core

---

## Summary

Plugins turn Wayang from:

> a workflow engine
> into
> a programmable orchestration platform.

All domain logic belongs in plugins.
The core only coordinates, persists, and enforces.

---
