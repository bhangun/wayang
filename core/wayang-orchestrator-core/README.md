# Wayang :: Core :: Orchestrator Core

Core module for Wayang orchestration engine, providing common logic for Gamelan integration.

## Architecture

This module provides the foundation for transport-agnostic orchestration. It is part of a three-tier architecture:

1.  **`wayang-orchestrator-core`** (This Module): Contains transport-agnostic logic for workflow deployment, run management, and configuration.
2.  **`wayang-orchestrator-local`**: Lightweight implementation for direct, in-process engine integration.
3.  **`wayang-orchestrator-remote`**: Lightweight implementation for remote REST/gRPC engine integration.

## Key Components

- **`AbstractGamelanWorkflowEngine`**: Encapsulates the core logic for mapping Wayang `RouteDesign` objects to Gamelan `WorkflowDefinition` objects.
- **`GamelanWorkflowRunManager`**: Handles workflow run lifecycle operations (create, get, resume, cancel).
- **`GamelanEngineConfig`**: Unified configuration mapping for Gamelan connection parameters.
- **`GamelanService`**: Diagnostic service for connectivity testing.

## Usage

Extend `AbstractGamelanWorkflowEngine` and initialize the `GamelanClient` with the desired transport:

```java
public class MyEngine extends AbstractGamelanWorkflowEngine {
    public MyEngine(GamelanEngineConfig config) {
        super(config);
        this.client = GamelanClient.builder()
            // ... transport configuration ...
            .build();
    }
}
```

## Integrity and Deduplication

By centralizing the logic for node mapping and lifecycle management in this core module, we ensure that:
- Workflows are deployed consistently regardless of the transport used.
- Architectural integrity is preserved as the orchestration logic remains separated from transport details.
- Maintenance is simplified by having a single point of truth for Wayang-to-Gamelan mapping.
