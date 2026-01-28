# Wayang Platform — Security

This document defines the security model of the Wayang Platform.

Wayang is designed as a distributed control plane and execution fabric.  
Security is enforced at every layer: API, workflow, executor, and plugin.

---

## 1. Security Principles

Wayang follows these core principles:

- Zero-trust by default
- Explicit identity for all actors
- Least-privilege access
- Defense in depth
- Auditable actions
- Secure-by-design extensibility

---

## 2. Identity Model

Identities in Wayang include:

- Users (humans)
- Services
- Executors
- Agents
- Plugins

Each identity:
- Is uniquely identifiable
- Is authenticated
- Is authorized
- Is auditable

---

## 3. Authentication

Supported authentication mechanisms:

- OIDC / OAuth2
- JWT tokens
- API keys (limited scope)
- mTLS (for executors and internal services)

Authentication is required for:
- Control Plane APIs
- Executor registration
- Task execution
- Plugin interaction

---

## 4. Authorization

Wayang supports:

- Role-Based Access Control (RBAC)
- Attribute-Based Access Control (ABAC)
- Policy-driven authorization

Authorization is enforced on:

- Workflow operations
- Node execution
- Approval actions
- Executor invocation
- State access

---

## 5. Workflow-Level Security

Each workflow may define:

- Who can start it
- Who can approve steps
- Which executors may run nodes
- Which data is accessible

Policies are evaluated:
- Before execution
- At transitions
- On approvals

---

## 6. Executor Trust Model

Executors must:

- Authenticate to Wayang
- Register capabilities
- Validate task signatures
- Respect assigned scopes

Wayang verifies:
- Executor identity
- Capability authorization
- Token ownership

---

## 7. Secret Management

Secrets are:

- Never embedded in workflow definitions
- Never stored in plain text in state
- Resolved at execution time

Supported secret sources:

- Vault systems
- Cloud secret managers
- Encrypted environment stores

Secrets are:
- Scoped per workflow or node
- Injected only into authorized executors

---

## 8. Plugin Security

Plugins:

- Declare required permissions
- Run in isolated contexts (where possible)
- Cannot access core state directly
- Use restricted APIs

Sensitive plugins may require:
- Signature verification
- Trusted registries
- Manual approval

---

## 9. Network Security

Wayang supports:

- TLS for all external communication
- mTLS for internal components
- Network segmentation
- Firewall and service mesh integration

No component assumes:
- Trusted network
- Trusted caller

---

## 10. Event Security

Events:

- Are signed or authenticated
- Include correlation IDs
- Are filtered by policy
- May be encrypted in transit

Event consumers are:
- Authenticated
- Authorized
- Audited

---

## 11. Audit & Compliance

Wayang records:

- Workflow executions
- State transitions
- Approval decisions
- Policy evaluations
- Executor actions

Audit logs are:
- Immutable
- Timestamped
- Correlated
- Exportable

---

## 12. Failure & Abuse Handling

Wayang protects against:

- Replay attacks (via idempotency)
- Executor spoofing
- Plugin abuse
- Privilege escalation

Handled via:
- Token validation
- Capability scoping
- Rate limiting
- Quarantine of faulty components

---

## 13. Multi-Tenancy Security

Tenants are isolated by:

- Namespace
- Policy
- Identity scope
- State store partitioning
- Executor scoping

No tenant can:
- Access another tenant’s workflows
- Execute another tenant’s nodes
- Read another tenant’s state

---

## 14. Human-in-the-Loop Security

Human actions (approvals, negotiation):

- Require authentication
- Are logged
- May require multi-party approval
- Can be time-limited

UI access is governed by:
- Roles
- Policies
- Workflow definitions

---

## 15. AI & Agent Security

Agents:

- Are treated as executors
- Have limited tool access
- Are policy-constrained
- May require human override

Agent outputs:
- Are validated
- Are auditable
- May require approval

---

## 16. Secure Defaults

Default configuration includes:

- Authentication required
- Authorization enforced
- Encrypted communication
- Auditing enabled
- Minimal plugin permissions

---

## Summary

Security in Wayang is not a feature.  
It is part of the execution model.

Wayang ensures:
- Only trusted actors execute
- Only permitted actions occur
- All decisions are traceable
- Failures are contained

Security is enforced at:
- Identity level
- Workflow level
- Executor level
- Plugin level
- Protocol level

---