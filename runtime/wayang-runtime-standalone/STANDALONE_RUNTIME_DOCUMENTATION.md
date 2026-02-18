# Wayang Standalone Runtime

The Wayang Standalone Runtime is a self-contained, embeddable runtime that combines all core components of the Wayang platform into a single executable JAR. This runtime is ideal for development, testing, edge computing, and desktop applications.

## Architecture Overview

The standalone runtime includes:

- **Control Plane**: Project, workflow, and agent management
- **Orchestration Engine**: Workflow execution and state management (Gamelan)
- **Inference Engine**: LLM and model inference (Gollek)
- **Plugin System**: Dynamic plugin loading and management
- **Schema Validation**: Data validation and schema management
- **Embedded Database**: H2 for metadata storage
- **Embedded State Store**: In-memory store for execution state

## Key Features

### Self-Contained
- Single JAR with all dependencies
- No external services required
- Embedded database and state store
- Local model inference

### Full Feature Set
- Complete control plane functionality
- Workflow orchestration
- LLM inference
- Plugin management
- Schema validation
- Multi-tenancy support (optional)

### Development Friendly
- Rapid startup times
- No complex setup required
- Ideal for prototyping and testing

## Getting Started

### Prerequisites
- Java 25 or higher
- Maven 3.8+ (for building from source)

### Running the Standalone Runtime

#### From Prebuilt JAR
```bash
java -jar wayang-runtime-standalone-{version}.jar
```

#### From Source
```bash
cd wayang/runtime/wayang-runtime-standalone
./mvnw clean package quarkus:dev
```

### Configuration

The runtime is configured via `application.properties`. Key configuration options:

```properties
# HTTP Port
quarkus.http.port=8080

# Database (H2 embedded by default)
quarkus.datasource.db-kind=h2

# Enable multitenancy (disabled by default)
wayang.multitenancy.enabled=false

# Plugin auto-discovery
wayang.plugins.auto-discovery=true

# Schema validation
wayang.schema.validation.enabled=true
```

## Component Integration

### Control Plane Services
- `ProjectManager`: Manages projects and workspaces
- `WorkflowManager`: Manages workflow templates and definitions
- `AgentManager`: Manages AI agents
- `SchemaRegistryService`: Manages schema registration and validation

### Orchestration Engine (Gamelan)
- `GamelanService`: Coordinates workflow execution
- Embedded workflow engine for local execution
- State management with in-memory store

### Inference Engine (Gollek)
- `GollekInferenceService`: Manages LLM and model inference
- Supports local and remote model providers
- Streaming and batch inference capabilities

### Plugin System
- `PluginRegistry`: Discovers and manages plugins
- `PluginManagerService`: Handles plugin lifecycle
- Supports executor, tool, and provider plugins

### Schema Validation
- `SchemaValidationService`: Validates data against schemas
- `SchemaRegistryService`: Stores and retrieves schemas
- Supports custom validation rules

## API Endpoints

### Control Plane APIs
- `GET /api/projects` - List projects
- `POST /api/projects` - Create project
- `GET /api/workflows` - List workflows
- `POST /api/workflows` - Create workflow
- `GET /api/agents` - List agents
- `POST /api/agents` - Create agent

### Orchestration APIs
- `GET /api/orchestration/workflows` - List workflow definitions (Gamelan)

### Inference APIs
- `POST /api/inference` - Execute inference
- `POST /api/inference/stream` - Streaming inference

### Runtime Integration APIs
- `GET /api/runtime/status` - Unified readiness snapshot across Wayang, Gamelan, and Gollek

## Configuration Options

### Database Configuration
```properties
# H2 (default for standalone)
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:wayangdb

# PostgreSQL (for production)
# quarkus.datasource.db-kind=postgresql
# quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/wayang
```

### Inference Configuration
```properties
# Local inference (default)
gollek.inference.local.enabled=true
gollek.inference.default-provider=local

# Remote provider
# gollek.inference.default-provider=openai
# gollek.openai.api-key=${OPENAI_API_KEY}
```

### Plugin Configuration
```properties
# Auto-discover plugins
wayang.plugins.auto-discovery=true
wayang.plugins.scan-packages=tech.kayys.wayang

# Plugin directory
wayang.plugins.directory=plugins
```

## Development

### Building from Source
```bash
# Build the entire platform
mvn clean install -DskipTests

# Build only the standalone runtime
cd wayang/runtime/wayang-runtime-standalone
mvn clean package
```

### Running in Development Mode
```bash
cd wayang/runtime/wayang-runtime-standalone
./mvnw quarkus:dev
```

### Adding New Components
New components can be added by:
1. Creating the service implementation
2. Adding it to the dependency injection system
3. Updating the main application class to initialize it
4. Adding configuration options if needed

## Use Cases

### Development & Testing
- Rapid prototyping
- Local development environment
- Unit and integration testing

### Edge Computing
- IoT devices
- Edge servers
- Mobile applications

### Desktop Applications
- Desktop AI tools
- Local workflow designers
- Offline AI capabilities

### Single-User Applications
- Personal AI assistants
- Local data processing
- Privacy-focused applications

## Performance Considerations

- The standalone runtime is optimized for single-user scenarios
- For multi-user or high-throughput scenarios, consider the distributed deployment
- Memory usage scales with the number of concurrent workflows
- Local model inference performance depends on hardware capabilities

## Security Considerations

- Authentication and authorization should be enabled for production use
- API keys should be properly secured
- Network access should be restricted in production
- Regular security updates are important

## Troubleshooting

### Common Issues
- Port conflicts: Check if port 8080 is available
- Database issues: Verify H2 dependencies are present
- Plugin loading: Ensure plugin JARs are in the correct directory

### Logging
- Set log level to DEBUG for detailed troubleshooting
- Check application logs in the standard output
- Monitor memory and CPU usage during heavy loads

## Extending the Runtime

The standalone runtime can be extended by:
- Adding custom plugins
- Implementing new service interfaces
- Creating custom API endpoints
- Adding new configuration options
