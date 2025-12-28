# API Reference

The Wayang Workflow Engine exposes both **gRPC** (primary) and **REST** (compatibility) APIs.

## Authentication

All APIs (gRPC and REST) require authentication. Two methods are supported:

### 1. Bearer Token (JWT)
The primary method, integrated with Keycloak.
- **Header**: `Authorization: Bearer <token>`
- **Tenant Resolution**: Extracted from the `tenant_id` claim or user principal.

### 2. Basic Authentication
Supported for development and internal tools.
- **Header**: `Authorization: Basic <base64(username:password)>`
- **Default Credentials**: `admin:admin` or `user:user`.

---

## gRPC Services
Default Port: `9090`

### 1. WorkflowRegistryService
Manage workflow definitions.
- `RegisterWorkflow`: Create/Update a workflow.
- `GetWorkflow`: Retrieve a definition.
- `ListWorkflows`: List all workflows.

### 2. WorkflowRunService
Manage execution instances.
- `CreateRun`: Initialize a new run.
  - Required: `workflow_id`
  - Optional: `workflow_version` (defaults to latest if empty), `inputs` (map)
- `GetRun`: Get run status and metadata.
- `StartRun` / `SuspendRun` / `ResumeRun` / `CancelRun`: Lifecycle management.

### 3. WorkflowSchedulerService
... (rest of services)

---

## Protobuf Definitions
The source of truth for the API is the `.proto` files located in `src/main/proto/`.
- `workflow_run.proto`: Contains `CreateRunRequest` with `workflow_version`.
- `workflow_registry.proto`
- ... (and others)
