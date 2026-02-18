# Wayang AI Agent Workflow Platform - Core Implementation Guide

Based on the comprehensive blueprint, I'll provide a **production-ready, modular microservices architecture** for the Wayang AI Agent Workflow Platform using **Quarkus 3.x** with modern best practices.'

---


### **FEATURES**

=

# Shared libraries & contracts
# API Gateway & Auth
# Workflow Designer Service
# Workflow Orchestrator
# Node Executor Service
# Planning Engine
# Tool Gateway (MCP)
# RAG & Memory Service
# Model Router & LLM Runtime
# Safety & Compliance
# Metrics & Tracing
# Standalone Agent Generator
# Plugin Manager


# Wayang Platform - Modular Architecture for Dynamic Loading

## Key Architectural Principles

### Approach 1: Full Platform (Dynamic Component Loading)
- **All components available** in the runtime classpath
- **Dynamic instantiation** based on workflow schema
- Components are loaded **on-demand** but all libraries are present
- Suitable for: Enterprise deployments, multi-tenant SaaS

### Approach 2: Standalone/Generated Agent (Minimal Dependencies)
- **Code generation** based on workflow schema
- **Tree-shaking**: Only include used components
- **Minimal dependency tree** - no unused libraries
- Generates **lightweight, portable agents**
- Suitable for: Edge deployment, microservices, embedded systems, client applications

## Revised Module Structure

```
wayang-platform/
├── wayang-core/                          # Minimal core interfaces (ALWAYS needed)
│   ├── wayang-api/                       # Core abstractions only
│   ├── wayang-spi/                       # Service Provider Interface
│   └── wayang-common/                    # Shared utilities
│
├── wayang-runtime/                       # Runtime execution engine
│   ├── wayang-runtime-core/              # Base runtime (ALWAYS needed)
│   ├── wayang-runtime-orchestrator/      # Workflow orchestration (if used)
│   └── wayang-runtime-executor/          # Node execution (ALWAYS needed)
│
├── wayang-nodes/                         # Individual node implementations
│   ├── wayang-node-agent/                # Agent node (independent JAR)
│   ├── wayang-node-rag/                  # RAG node (independent JAR)
│   ├── wayang-node-tool/                 # Tool node (independent JAR)
│   ├── wayang-node-guardrails/           # Guardrails node (independent JAR)
│   ├── wayang-node-evaluator/            # Evaluator node (independent JAR)
│   ├── wayang-node-critic/               # Critic node (independent JAR)
│   ├── wayang-node-decision/             # Decision node (independent JAR)
│   └── wayang-node-memory/               # Memory node (independent JAR)
│
├── wayang-services/                      # Backing services (pluggable)
│   ├── wayang-service-llm/               # LLM service (if AI nodes used)
│   ├── wayang-service-embedding/         # Embedding service (if RAG used)
│   ├── wayang-service-vector/            # Vector store (if RAG used)
│   ├── wayang-service-memory/            # Memory service (if memory used)
│   └── wayang-service-tool/              # Tool gateway (if tools used)
│
├── wayang-codegen/                       # Code generation for standalone
│   ├── wayang-codegen-core/              # Code generation engine
│   ├── wayang-codegen-analyzer/          # Schema analyzer
│   ├── wayang-codegen-optimizer/         # Dependency optimizer
│   └── wayang-codegen-templates/         # Code templates
│
├── wayang-designer/                      # Visual workflow designer (UI)
│   └── wayang-designer-backend/          # Backend API for designer
│
└── wayang-platform-full/                 # Full platform assembly
    └── pom.xml                           # Aggregates all modules
```






###############################################################################
# Wayang AI Agent Platform - Complete Project Structure
###############################################################################

# Root Project Structure
wayang-platform/
├── pom.xml (parent)
├── README.md
├── LICENSE
├── .github/
│   └── workflows/
│       ├── build.yml
│       ├── security-scan.yml
│       └── deploy.yml
│
├── docs/
│   ├── architecture/
│   ├── api/
│   └── deployment/
│
├── schemas/
│   ├── PluginDescriptor.schema.json
│   ├── ErrorPayload.schema.json
│   └── AuditEvent.schema.json
│
├── modules/
│   │
│   ├── common/ (Shared modules)
│   │   ├── plugin-common/
│   │   │   ├── src/main/java/io/wayang/plugin/common/
│   │   │   │   ├── ErrorPayload.java
│   │   │   │   ├── ExecutionResult.java
│   │   │   │   ├── NodeContext.java
│   │   │   │   └── PluginDescriptor.java
│   │   │   └── pom.xml
│   │   │
│   │   ├── plugin-api/
│   │   │   ├── src/main/java/io/wayang/plugin/api/
│   │   │   │   ├── PluginRegistryClient.java
│   │   │   │   └── PluginLoaderClient.java
│   │   │   └── pom.xml
│   │   │
│   │   └── plugin-spi/
│   │       ├── src/main/java/io/wayang/plugin/spi/
│   │       │   ├── Node.java
│   │       │   ├── NodeFactory.java
│   │       │   └── IsolationStrategy.java
│   │       └── pom.xml
│   │
│   ├── services/ (Microservices)
│   │   │
│   │   ├── plugin-registry-service/
│   │   │   ├── src/
│   │   │   │   ├── main/
│   │   │   │   │   ├── java/io/wayang/plugin/registry/
│   │   │   │   │   │   ├── PluginRegistryResource.java
│   │   │   │   │   │   ├── PluginRegistryService.java
│   │   │   │   │   │   ├── PluginRepository.java
│   │   │   │   │   │   ├── PluginEntity.java
│   │   │   │   │   │   ├── PluginValidationService.java
│   │   │   │   │   │   └── PluginEventEmitter.java
│   │   │   │   │   ├── resources/
│   │   │   │   │   │   ├── application.yml
│   │   │   │   │   │   └── db/migration/
│   │   │   │   │   │       └── V1__create_plugins_table.sql
│   │   │   │   │   └── docker/
│   │   │   │   │       └── Dockerfile.jvm
│   │   │   │   └── test/
│   │   │   ├── pom.xml
│   │   │   └── README.md
│   │   │
│   │   ├── plugin-loader-service/
│   │   │   ├── src/
│   │   │   │   ├── main/
│   │   │   │   │   ├── java/io/wayang/plugin/loader/
│   │   │   │   │   │   ├── PluginLoaderResource.java
│   │   │   │   │   │   ├── PluginLoaderService.java
│   │   │   │   │   │   ├── PluginIsolationManager.java
│   │   │   │   │   │   ├── ClassLoaderIsolationStrategy.java
│   │   │   │   │   │   ├── WasmIsolationStrategy.java
│   │   │   │   │   │   ├── ContainerIsolationStrategy.java
│   │   │   │   │   │   ├── ErrorHandlerService.java
│   │   │   │   │   │   ├── RetryManager.java
│   │   │   │   │   │   └── SelfHealingService.java
│   │   │   │   │   └── resources/
│   │   │   │   │       └── application.yml
│   │   │   │   └── test/
│   │   │   └── pom.xml
│   │   │
│   │   ├── plugin-scanner-service/
│   │   │   ├── src/
│   │   │   │   ├── main/
│   │   │   │   │   ├── java/io/wayang/plugin/scanner/
│   │   │   │   │   │   ├── PluginScannerResource.java
│   │   │   │   │   │   ├── PluginScannerService.java
│   │   │   │   │   │   ├── VulnerabilityScannerService.java
│   │   │   │   │   │   ├── LicenseCheckerService.java
│   │   │   │   │   │   └── SecretScannerService.java
│   │   │   │   │   └── resources/
│   │   │   │   │       └── application.yml
│   │   │   │   └── test/
│   │   │   └── pom.xml
│   │   │
│   │   ├── plugin-governance-service/
│   │   │   ├── src/
│   │   │   │   ├── main/
│   │   │   │   │   ├── java/io/wayang/plugin/governance/
│   │   │   │   │   │   ├── PluginGovernanceResource.java
│   │   │   │   │   │   ├── ApprovalWorkflowService.java
│   │   │   │   │   │   ├── PolicyEngineService.java
│   │   │   │   │   │   └── HITLService.java
│   │   │   │   │   └── resources/
│   │   │   │   │       └── application.yml
│   │   │   │   └── test/
│   │   │   └── pom.xml
│   │   │
│   │   ├── plugin-audit-service/
│   │   │   ├── src/
│   │   │   │   ├── main/
│   │   │   │   │   ├── java/io/wayang/plugin/audit/
│   │   │   │   │   │   ├── PluginAuditService.java
│   │   │   │   │   │   ├── AuditRepository.java
│   │   │   │   │   │   ├── AuditEventEntity.java
│   │   │   │   │   │   └── ProvenanceService.java
│   │   │   │   │   └── resources/
│   │   │   │   │       ├── application.yml
│   │   │   │   │       └── db/migration/
│   │   │   │   │           └── V1__create_audit_table.sql
│   │   │   │   └── test/
│   │   │   └── pom.xml
│   │   │
│   │   └── artifact-store-service/
│   │       ├── src/
│   │       │   ├── main/
│   │       │   │   ├── java/io/wayang/plugin/artifact/
│   │       │   │   │   ├── ArtifactStoreResource.java
│   │       │   │   │   ├── ArtifactStorageService.java
│   │       │   │   │   └── ArtifactCacheService.java
│   │       │   │   └── resources/
│   │       │   │       └── application.yml
│   │       │   └── test/
│   │       └── pom.xml
│   │
│   └── runtime/ (Standalone runtime components)
│       │
│       ├── plugin-runtime-lite/
│       │   ├── src/main/java/io/wayang/plugin/runtime/
│       │   │   ├── LitePluginLoader.java
│       │   │   ├── LiteErrorHandler.java
│       │   │   └── LiteAuditLogger.java
│       │   └── pom.xml
│       │
│       └── plugin-loader-lite/
│           ├── src/main/java/io/wayang/plugin/loader/lite/
│           │   └── StandalonePluginLoader.java
│           └── pom.xml
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── kubernetes/
│   │   ├── namespace.yaml
│   │   ├── postgres.yaml
│   │   ├── redis.yaml
│   │   ├── kafka.yaml
│   │   ├── plugin-registry-deployment.yaml
│   │   ├── plugin-loader-deployment.yaml
│   │   └── ingress.yaml
│   └── terraform/
│       └── main.tf
│
└── examples/
    ├── sample-plugins/
    │   ├── data-processor-plugin/
    │   └── validation-plugin/
    └── standalone-agents/
        └── simple-agent/





## 3. Project Structure (Modular Multi-Module Maven)

```
wayang-platform/
│
├── pom.xml                          # Parent POM
│
├── wayang-common/                   # Shared utilities & models
│   ├── wayang-common-core/          # Core interfaces, DTOs
│   ├── wayang-common-security/      # Security utilities
│   └── wayang-common-errors/        # Error handling framework
│
├── wayang-runtime/                  # Shared runtime (platform + standalone)
│   ├── wayang-runtime-core/         # Node executor, orchestrator engine
│   ├── wayang-runtime-guardrails/   # Guardrails engine
│   ├── wayang-runtime-tools/        # MCP tool execution
│   ├── wayang-runtime-rag/          # RAG components
│   ├── wayang-runtime-memory/       # Memory services
│   └── wayang-runtime-llm/          # LLM abstraction layer
│
├── wayang-services/                 # Microservices
│   ├── api-gateway/                 # API Gateway service
│   ├── auth-service/                # Authentication (Keycloak wrapper)
│   ├── designer-service/            # Workflow designer
│   ├── schema-registry-service/     # Node schema registry
│   ├── version-service/             # Version control
│   ├── plugin-manager-service/      # Plugin lifecycle
│   ├── orchestrator-service/        # Main orchestrator
│   ├── planner-service/             # Planning engine
│   ├── executor-service/            # Node executor pool manager
│   ├── llm-runtime-service/         # LLM inference proxy
│   ├── tool-gateway-service/        # MCP tool gateway
│   ├── rag-service/                 # RAG operations
│   ├── memory-service/              # Memory CRUD
│   ├── state-store-service/         # State persistence
│   ├── audit-service/               # Audit logging
│   ├── observability-service/       # Metrics aggregation
│   └── codegen-service/             # Standalone agent generator
│
├── wayang-tools/                    # MCP Tool Implementations
│   ├── wayang-tool-sdk/             # MCP SDK for tool authors
│   ├── wayang-tool-registry/        # Built-in tool registry
│   ├── tools-builtin/               # HTTP, DB, File, etc.
│   └── tools-examples/              # Example custom tools
│
├── wayang-plugins/                  # Plugin system
│   ├── plugin-api/                  # Plugin SPI
│   ├── plugin-loader/               # Hot-reload engine
│   └── plugins-core/                # Core node types
│
├── wayang-ui/                       # Frontend (separate repo or submodule)
│   └── designer-ui/                 # React/Vue workflow builder
│
├── wayang-standalone/               # Standalone agent template
│   └── agent-template/              # Minimal runtime template
│
├── wayang-deployment/               # Deployment artifacts
│   ├── docker/                      # Dockerfiles
│   ├── kubernetes/                  # Helm charts
│   └── terraform/                   # Infrastructure as Code
│
└── wayang-examples/                 # Example workflows
    ├── financial-reconciliation/
    ├── customer-support-agent/
    └── document-analysis/
```
