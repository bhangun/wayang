# Wayang Executors - Integration

This module contains integration executors for the Wayang platform, allowing it to connect and interact with various external systems and services.

## Structure

- `modules/` - Individual integration modules for different services/providers
- `wayang-integration-runtime/` - Unified runtime environment for executing integration workflows

## Supported Integrations

- **Cloud Providers**: AWS, Azure, Google Cloud Platform
- **CRM Systems**: Salesforce
- **Databases**: PostgreSQL, MongoDB, Redis
- **Messaging**: Apache Kafka
- **Storage**: Various cloud storage solutions

## Architecture

The integration executor follows a modular architecture where each service/provider has its own module. These modules can be combined in the runtime to create custom integration solutions.

## Building

To build the entire integration suite:

```bash
mvn clean install
```

To build individual components:

```bash
# Build all modules
cd modules
mvn clean install

# Build the runtime
cd wayang-integration-runtime
mvn clean install
```

## Deployment

The integration runtime can be deployed as:
- Standalone JAR application
- Docker container
- Kubernetes deployment (see kubernetes manifests)

## Configuration

The runtime is configured through environment variables or application.properties files. See the runtime README for detailed configuration options.