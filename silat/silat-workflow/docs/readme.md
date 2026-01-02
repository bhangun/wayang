# Silat Workflow Engine 🥋

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.15.1-blue)](https://quarkus.io/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)

> **Silat** - A production-ready, enterprise-grade workflow orchestration engine for agentic AI, enterprise integration patterns, and business automation.

Named after the martial art known for its fluid, adaptive movements, Silat embodies flexibility, resilience, and precision in workflow orchestration.

## 🌟 Key Features

### Core Capabilities
- **Event Sourcing**: Complete audit trail with event replay capability
- **CQRS Pattern**: Optimized command and query paths
- **Distributed Execution**: Scale horizontally with gRPC, Kafka, or REST
- **Multi-Tenancy**: First-class tenant isolation support
- **Reactive Architecture**: Built on Quarkus and Mutiny for high performance
- **State Machine**: Robust workflow state management with validation
- **Saga Pattern**: Automated compensation for distributed transactions
- **Optimistic Locking**: Concurrent execution without conflicts

### Enterprise Features
- **Service Discovery**: Consul, Kubernetes, or static configuration
- **Circuit Breakers**: Fault tolerance for executor failures
- **Distributed Locking**: Redis-based coordination
- **Observability**: OpenTelemetry, Prometheus metrics, structured logging
- **Security**: JWT/OIDC authentication, execution tokens, callback verification
- **Retry Policies**: Configurable exponential backoff
- **Task Scheduling**: Priority-based queuing with dead letter support

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Applications                       │
│                  (REST, gRPC, or SDK)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   API Layer (Silat API)                      │
│          ┌──────────────┬────────────────┐                  │
│          │  REST API    │   gRPC API     │                  │
│          └──────────────┴────────────────┘                  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Core Workflow Engine (Silat Core)               │
│  ┌────────────────┬──────────────────┬─────────────────┐   │
│  │ WorkflowRun    │  RunManager      │  Execution      │   │
│  │  Aggregate     │  (Orchestrator)  │   Engine        │   │
│  └────────────────┴──────────────────┴─────────────────┘   │
│  ┌────────────────┬──────────────────┬─────────────────┐   │
│  │ Event Store    │  Scheduler       │  Distributed    │   │
│  │                │                  │  Locking        │   │
│  └────────────────┴──────────────────┴─────────────────┘   │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Communication Layer (Kafka/gRPC)                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Executor Nodes                              │
│  ┌────────────┬────────────┬────────────┬────────────┐     │
│  │ Executor 1 │ Executor 2 │ Executor 3 │ Executor N │     │
│  │  (gRPC)    │  (Kafka)   │   (REST)   │    ...     │     │
│  └────────────┴────────────┴────────────┴────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Domain Model

```
WorkflowRun (Aggregate Root)
├── WorkflowRunId (Identity)
├── TenantId (Multi-tenancy)
├── RunStatus (State Machine)
├── ExecutionContext (Variables, Node States)
├── ExecutionEvents (Event Sourcing)
└── NodeExecutions
    ├── NodeExecution 1
    ├── NodeExecution 2
    └── NodeExecution N

WorkflowDefinition (Blueprint)
├── WorkflowDefinitionId
├── NodeDefinitions
│   ├── Task Nodes
│   ├── Decision Nodes
│   ├── Parallel Nodes
│   ├── Human Task Nodes
│   └── Sub-Workflow Nodes
├── Transitions (DAG)
├── RetryPolicy
└── CompensationPolicy
```

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+
- Kafka 3.5+ (optional)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/kayys/silat-workflow-engine.git
cd silat-workflow-engine
```

2. **Start infrastructure**
```bash
docker-compose up -d postgres redis kafka
```

3. **Build the project**
```bash
mvn clean install
```

4. **Run the engine**
```bash
cd silat-core
mvn quarkus:dev
```

The engine will start on:
- REST API: `http://localhost:8080`
- gRPC: `localhost:9090`
- Health: `http://localhost:8080/health`
- Metrics: `http://localhost:8080/metrics`

### Docker Compose Setup

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: silat_workflow
      POSTGRES_USER: silat
      POSTGRES_PASSWORD: silat123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  silat-engine:
    image: kayys/silat-workflow-engine:latest
    environment:
      DB_HOST: postgres
      REDIS_HOST: redis
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8080:8080"
      - "9090:9090"
    depends_on:
      - postgres
      - redis
      - kafka

volumes:
  postgres_data:
  redis_data:
```

## 📖 Usage Examples

### Define a Workflow

```java
WorkflowDefinition workflow = WorkflowDefinition.builder()
    .id(WorkflowDefinitionId.of("order-processing"))
    .name("Order Processing")
    .version("1.0.0")
    .addNode(NodeDefinition.builder()
        .id(NodeId.of("validate-order"))
        .name("Validate Order")
        .type(NodeType.TASK)
        .executorType("order-validator")
        .timeout(Duration.ofSeconds(30))
        .retryPolicy(RetryPolicy.DEFAULT)
        .build())
    .addNode(NodeDefinition.builder()
        .id(NodeId.of("process-payment"))
        .name("Process Payment")
        .type(NodeType.TASK)
        .executorType("payment-processor")
        .dependsOn(List.of(NodeId.of("validate-order")))
        .critical(true)
        .build())
    .addNode(NodeDefinition.builder()
        .id(NodeId.of("fulfill-order"))
        .name("Fulfill Order")
        .type(NodeType.TASK)
        .executorType("fulfillment-service")
        .dependsOn(List.of(NodeId.of("process-payment")))
        .build())
    .compensationPolicy(CompensationPolicy.DEFAULT)
    .build();

// Register the workflow
definitionRegistry.register(workflow, TenantId.of("acme-corp"));
```

### Create and Start a Workflow Run

```java
// Create run request
CreateRunRequest request = new CreateRunRequest(
    "order-processing",
    Map.of(
        "orderId", "ORDER-12345",
        "customerId", "CUST-67890",
        "items", List.of(
            Map.of("sku", "ITEM-001", "quantity", 2),
            Map.of("sku", "ITEM-002", "quantity", 1)
        ),
        "totalAmount", 299.99
    )
);

// Create the run
Uni<WorkflowRun> runUni = workflowRunManager.createRun(
    request,
    TenantId.of("acme-corp")
);

// Start execution
runUni.flatMap(run -> 
    workflowRunManager.startRun(run.getId(), run.getTenantId())
).subscribe().with(
    run -> System.out.println("Workflow started: " + run.getId().value()),
    failure -> System.err.println("Failed to start: " + failure.getMessage())
);
```

### Implement an Executor (gRPC)

```java
@ApplicationScoped
public class OrderValidatorExecutor implements WorkflowExecutor {
    
    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        // Extract inputs
        Map<String, Object> context = task.context();
        String orderId = (String) context.get("orderId");
        
        // Perform validation
        return validateOrder(orderId)
            .map(valid -> {
                if (valid) {
                    return NodeExecutionResult.success(
                        task.runId(),
                        task.nodeId(),
                        task.attempt(),
                        Map.of("validationResult", "PASSED"),
                        task.token()
                    );
                } else {
                    return NodeExecutionResult.failure(
                        task.runId(),
                        task.nodeId(),
                        task.attempt(),
                        ErrorInfo.of(new ValidationException("Invalid order")),
                        task.token()
                    );
                }
            });
    }
    
    @Override
    public String executorType() {
        return "order-validator";
    }
}
```

### Query Workflow Status

```java
// Get workflow run
Uni<WorkflowRun> runUni = workflowRunManager.getRun(
    WorkflowRunId.of("run-id"),
    TenantId.of("acme-corp")
);

// Get execution history
Uni<ExecutionHistory> historyUni = workflowRunManager.getExecutionHistory(
    WorkflowRunId.of("run-id"),
    TenantId.of("acme-corp")
);

// Query runs
Uni<List<WorkflowRun>> runsUni = workflowRunManager.queryRuns(
    TenantId.of("acme-corp"),
    WorkflowDefinitionId.of("order-processing"),
    RunStatus.RUNNING,
    0,  // page
    10  // size
);
```

## 🔧 Configuration

### Application Properties

Key configuration options:

```properties
# Engine Configuration
silat.engine.max-concurrent-executions=1000
silat.engine.default-workflow-timeout=PT1H
silat.engine.event-sourcing.enabled=true

# Multi-tenancy
silat.tenancy.isolation-level=DISCRIMINATOR
silat.tenancy.resolution-strategy=HEADER

# Communication Strategy
silat.executor.communication-strategy=AUTO

# Service Registry
silat.registry.type=consul
silat.registry.consul.host=localhost
```

See `application.yml` for complete configuration options.

## 🔐 Security

### Multi-Tenancy

Silat provides three isolation levels:

1. **DISCRIMINATOR**: Shared database with tenant_id column (default)
2. **SCHEMA**: Separate schema per tenant
3. **DATABASE**: Separate database per tenant

### Authentication

Supports:
- JWT (recommended)
- OIDC
- API Keys (for executors)

### Execution Tokens

Every node execution requires a valid execution token:
- Generated by RunManager
- Time-limited (configurable)
- Cryptographically secure
- Validated on result submission

## 📊 Monitoring & Observability

### Metrics

Exposes Prometheus metrics:
- Workflow execution rate
- Success/failure rates
- Execution duration (p50, p95, p99)
- Active workflows count
- Task queue depth

### Distributed Tracing

OpenTelemetry integration:
- Trace workflow execution across services
- Correlate with external systems
- Performance profiling

### Health Checks

- **Liveness**: `/health/live`
- **Readiness**: `/health/ready`
- **Startup**: `/health/started`

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Load tests (requires JMeter)
mvn jmeter:jmeter
```

## 📦 Deployment

### Kubernetes

```bash
# Build container
mvn clean package -Dquarkus.container-image.build=true

# Deploy to Kubernetes
kubectl apply -f k8s/
```

### Native Image

```bash
# Build native executable
mvn package -Pnative

# Run native executable
./target/silat-core-1.0.0-SNAPSHOT-runner
```

## 🛣️ Roadmap

- [ ] Visual workflow designer (Web UI)
- [ ] Temporal integration
- [ ] State machine visualization
- [ ] Advanced analytics dashboard
- [ ] AI-powered workflow optimization
- [ ] Multi-cloud support (AWS Step Functions, Azure Logic Apps compatibility)
- [ ] Workflow versioning and migration tools

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

Apache License 2.0 - see [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by AWS Step Functions, Temporal, and Camunda
- Built with Quarkus, the Supersonic Subatomic Java Framework
- Event Sourcing patterns from Greg Young and Martin Fowler

## 📞 Support

- **Documentation**: [https://docs.silat.dev](https://docs.silat.dev)
- **Issues**: [GitHub Issues](https://github.com/kayys/silat/issues)
- **Discussions**: [GitHub Discussions](https://github.com/kayys/silat/discussions)
- **Email**: support@kayys.tech

---

**Built with ❤️ by the Kayys Team**