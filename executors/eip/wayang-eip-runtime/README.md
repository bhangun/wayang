# Wayang Integration Runtime

The Wayang Integration Runtime is a unified execution environment for running integration workflows that connect to various external systems and services.

## Overview

The runtime provides a flexible and scalable platform for executing integration tasks across multiple cloud providers and data systems. It supports:

- **Cloud Providers**: AWS, Azure, Google Cloud Platform
- **CRM Systems**: Salesforce
- **Message Brokers**: Apache Kafka
- **Databases**: PostgreSQL, MongoDB
- **Caching**: Redis

## Architecture

The runtime consists of:
- A Quarkus-based application server
- Apache Camel for integration routing
- Modular plugin system for different integration types
- Configuration-driven module loading

## Configuration

The runtime can be configured through environment variables or the `application.properties` file:

```properties
# Engine Connection
wayang.integration.engine.grpc.host=wayang-engine-service
wayang.integration.engine.grpc.port=9090
wayang.integration.executor.id=integration-executor-01

# Enabled Modules
wayang.integration.enabled.modules=aws,salesforce,azure,gcp,kafka,mongodb,postgresql,redis

# Port
quarkus.http.port=8082
```

## Building

```bash
mvn clean install
```

## Running

### Local Development
```bash
mvn quarkus:dev
```

### Production
```bash
java -jar target/wayang-integration-runtime-runner.jar
```

## Docker

To build a Docker image:

```bash
mvn package -Dquarkus.container-image.build=true
```

## Endpoints

- `/integration/health` - Health check
- `/integration/info` - Runtime information
- `/q/health` - Quarkus health endpoints
- `/q/metrics` - Metrics endpoint
- `/q/swagger-ui` - API documentation