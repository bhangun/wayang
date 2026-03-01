# Wayang Standalone Runtime

The Wayang Standalone Runtime is a self-contained, embeddable runtime that combines all core components of the Wayang platform into a portable executable distribution. It can be packaged as a single executable JAR or as a native binary.

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

#### From Prebuilt Portable JAR
```bash
java -jar wayang-runtime-standalone-{version}-runner.jar
```

#### Mode Selection
Community mode is default (`quarkus.profile=community`) and uses embedded H2 in PostgreSQL compatibility mode.

Enterprise mode uses external PostgreSQL:
```bash
java -Dquarkus.profile=enterprise \
  -DWAYANG_DB_JDBC_URL=jdbc:postgresql://localhost:5432/wayang \
  -DWAYANG_DB_USERNAME=wayang \
  -DWAYANG_DB_PASSWORD=wayang \
  -DWAYANG_DB_REACTIVE_URL=postgresql://localhost:5432/wayang \
  -jar wayang-runtime-standalone-{version}-runner.jar
```

#### From Source
```bash
cd wayang/runtime/wayang-runtime-standalone
./mvnw clean package quarkus:dev
```

### Build Artifacts

#### Portable JAR (single file)
```bash
mvn -f wayang/pom.xml -pl runtime/wayang-runtime-standalone -am clean package -DskipTests
```



Artifact:
- `target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner.jar`

#### Native Binary
```bash
mvn -f wayang/pom.xml -pl runtime/wayang-runtime-standalone -am clean package -Dnative -DskipTests
```

Artifact:
- `target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner`

Note:
- Native profile excludes `gollek-sdk-java-local` and `hypersistence-utils-hibernate-63` to keep native-image compilation stable.

### Configuration

The runtime is configured via `application.properties`. Key configuration options:

```properties
# HTTP Port
quarkus.http.port=31713

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

### Discovery and Docs
- `GET /api-index` - Unified endpoint index
- `GET /q/openapi` - OpenAPI spec
- `GET /q/swagger-ui` - Swagger UI

### Orchestration APIs
- `GET /api/orchestration/workflows` - List workflow definitions (Gamelan)
- `GET /api/v1/workflow-definitions` - Workflow definitions API
- `GET /api/v1/workflow-runs` - Workflow runtime API

### Inference APIs
- `POST /api/inference` - Execute inference
- `POST /api/inference/stream` - Streaming inference
- `GET /v1/converter/gguf/health` - GGUF native converter health (`native` or `degraded`)

### Executors APIs
- `GET /api/v1/executors` - List registered executors
- `GET /api/v1/executors?healthy=true` - List healthy executors only

### Schema APIs (GUI/Designer)
- `GET /api/v1/schema/catalog` - List available built-in schemas
- `GET /api/v1/schema/catalog/{schemaId}` - Get schema JSON by id
- `POST /api/v1/schema/catalog/{schemaId}/validate` - Validate payload with selected schema

### Runtime Integration APIs
- `GET /api/runtime/status` - Unified readiness snapshot across Wayang, Gamelan, and Gollek
- `POST /api/runtime/shutdown` - Graceful shutdown trigger for standalone runtime process
- `GET /q/health` - Server health endpoint used by runtime monitor UI

### Designer Runtime Monitor
- `wayang-ui/wayang_designer` includes a **Runtime Monitor** dialog in the top toolbar (`heart monitor` icon).
- It reads:
  - `GET /q/health` for server health
  - `GET /api/runtime/status` for component readiness
- It can trigger shutdown via:
  - `POST /api/runtime/shutdown`

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

```bash
java -Dgamelan.tenant.default-id=community -Dgamelan.tenant.allow-default=true -jar //wayang/runtime/wayang-runtime-standalone/target/wayang-runtime-standalone-1.0.0-SNAPSHOT-runner.jar
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

## GGUF Converter Degraded Mode

When `gguf_bridge` native library is not available, standalone runtime starts in degraded mode for GGUF converter only.

- Runtime stays up (no crash).
- `GET /v1/converter/gguf/health` returns:
  - `nativeAvailable=false`
  - `mode=degraded`
  - `reason=<native load error>`
- Native GGUF conversion operations (`/convert`, `/verify`, `/model-info`) will return controlled errors until library is available.

### Enable Full GGUF Native Mode

Provide `gguf_bridge` dynamic library path before launching runtime.

macOS:
```bash
export DYLD_LIBRARY_PATH=/path/to/gguf-bridge:$DYLD_LIBRARY_PATH
```

Linux:
```bash
export LD_LIBRARY_PATH=/path/to/gguf-bridge:$LD_LIBRARY_PATH
```

Then restart runtime and verify:
```bash
curl http://localhost:31713/v1/converter/gguf/health
```

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
