# Wayang Control Plane - Synchronized Architecture

This document describes the synchronized architecture of the Wayang Control Plane modules, including the SPI, Core, API, and gRPC components.

## Overview

The Wayang Control Plane provides centralized management for projects, workflows, agents, and schemas in the Wayang platform. The architecture follows a modular design with clear separation of concerns:

- **wayang-control-spi**: Service Provider Interface definitions
- **wayang-control-core**: Core business logic implementations
- **wayang-control-api**: REST API endpoints
- **wayang-control-grpc**: gRPC service implementations

## Architecture Components

### 1. wayang-control-spi (Service Provider Interface)

The SPI module defines the interfaces for control plane services:

- `ProjectManagerSpi` - Interface for project management
- `WorkflowManagerSpi` - Interface for workflow management
- `AgentManagerSpi` - Interface for agent management
- `SchemaRegistrySpi` - Interface for schema registry
- `PluginManagerSpi` - Interface for plugin management

All implementations in the core module implement these interfaces to ensure consistency and allow for alternative implementations.

### 2. wayang-control-core (Core Implementation)

The core module contains the business logic implementations:

- `ProjectManager` - Manages projects and workspaces
- `WorkflowManager` - Manages workflow templates and definitions
- `AgentManager` - Manages AI agents
- `SchemaRegistryService` - Manages schema registration and validation
- `PluginManagerService` - Manages plugins

These services use reactive programming with Mutiny for asynchronous operations and integrate with the database using Hibernate Reactive.

### 3. wayang-control-api (REST API)

The API module provides REST endpoints for external consumption:

- `/v1/projects` - Project management endpoints
- `/v1/workflows` - Workflow management endpoints
- `/v1/agents` - Agent management endpoints
- `/v1/schemas` - Schema registry endpoints

All endpoints follow REST conventions and return JSON responses.

### 4. wayang-control-grpc (gRPC Service)

The gRPC module provides high-performance binary protocol services:

- `ProjectService` - gRPC service for project management
- `WorkflowService` - gRPC service for workflow management
- `AgentService` - gRPC service for agent management
- `SchemaRegistryService` - gRPC service for schema operations

## Integration with Schema Validation System

The control plane is tightly integrated with the schema validation system:

- Schema validation occurs during workflow and agent creation
- The SchemaRegistryService manages schema registration and validation
- Validation results are returned consistently across all interfaces
- Custom validation rules can be applied to different schema types

## Integration with Plugin System

The control plane integrates with the plugin system:

- PluginManagerService handles plugin lifecycle
- Plugins can register custom schemas through the schema registry
- Plugin configurations are validated using the schema validation system

## Data Flow

### Project Creation Flow
1. REST API receives `POST /v1/projects`
2. API calls `ProjectManager.createProject()`
3. Service validates request against schema
4. Service persists project to database
5. Response returned to client

### Workflow Publishing Flow
1. REST API receives `POST /v1/workflows/{id}/publish`
2. API calls `WorkflowManager.publishWorkflowTemplate()`
3. Service validates workflow against schema
4. Service publishes to orchestration engine
5. Response returned with workflow definition ID

### Agent Execution Flow
1. REST API receives `POST /v1/agents/{id}/execute`
2. API calls `AgentManager.executeTask()`
3. Service validates task parameters
4. Service executes task with agent
5. Response returned with execution results

## Module Dependencies

```
wayang-control-api
├── wayang-control-core
├── wayang-control-spi
├── quarkus-resteasy
└── quarkus-resteasy-jackson

wayang-control-grpc
├── wayang-control-core
├── wayang-control-spi
├── grpc-netty-shaded
└── grpc-protobuf

wayang-control-core
├── wayang-plugin-registry
├── wayang-plugin-spi
├── wayang-agent-core
├── wayang-schema
├── wayang-schema-api
├── wayang-control-spi
├── quarkus-hibernate-reactive-panache
└── quarkus-reactive-pg-client
```

## Security Considerations

- All API endpoints should be protected with authentication/authorization
- Tenant isolation is enforced through tenant ID parameters
- Sensitive data (credentials, tokens) should be encrypted
- API rate limiting should be implemented at the gateway level

## Performance Considerations

- Use reactive programming patterns throughout
- Implement proper caching for frequently accessed data
- Use connection pooling for database access
- Optimize schema validation for performance

## Error Handling

- Consistent error response format across all interfaces
- Proper HTTP status codes for REST API
- gRPC status codes for gRPC services
- Comprehensive logging for debugging

## Configuration

- Database connection settings
- Schema validation rules
- Plugin loading configuration
- API rate limiting settings

## Extension Points

The SPI architecture allows for:

- Alternative project management implementations
- Custom workflow storage backends
- Different schema validation engines
- Custom plugin management strategies

## Testing Strategy

- Unit tests for core services
- Integration tests for API endpoints
- Contract tests for gRPC services
- End-to-end tests for complete flows