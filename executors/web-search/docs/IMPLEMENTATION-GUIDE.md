# Wayang AI Agent Workflow Builder - Complete Implementation Guide

## Project: Web Search Executor Module

**Company**: kayys.tech  
**Platform**: Wayang AI Agent Workflow Builder  
**Version**: 1.0.0  
**Architecture**: Microservices with Multi-Tenancy  
**Framework**: Quarkus 3.x+  

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Complete Module Structure](#complete-module-structure)
3. [Technology Stack](#technology-stack)
4. [Core Implementation Files](#core-implementation-files)
5. [Provider Implementations](#provider-implementations)
6. [Deployment Architecture](#deployment-architecture)
7. [Multi-Tenancy Implementation](#multi-tenancy-implementation)
8. [Error Handling & Audit](#error-handling--audit)
9. [Testing Strategy](#testing-strategy)
10. [CI/CD Pipeline](#cicd-pipeline)

---

## Project Overview

The Web Search Executor is a modular, extensible component within the Wayang platform that provides:

- **Multi-provider search** (Google, Bing, DuckDuckGo, SerpAPI, Brave, Custom)
- **Multiple search capabilities** (text, image, video, news, academic, shopping, maps, code)
- **Content enhancement pipeline** (extraction, NER, summarization, translation, screenshots)
- **Intelligent caching and rate limiting**
- **Full observability** (OpenTelemetry, Prometheus metrics, distributed tracing)
- **Complete audit trails** for compliance
- **Portable runtime** for standalone agents

### Key Design Principles

1. **Modularity**: Each capability is a separate module
2. **Provider Agnostic**: Easy to add new search providers
3. **Multi-Tenancy Native**: Tenant isolation at every layer
4. **Observable**: Complete visibility into operations
5. **Resilient**: Circuit breakers, retries, fallbacks
6. **Cost-Aware**: Track and optimize search API costs

---

## Complete Module Structure

```
wayang-platform/
├── pom.xml                                 # Root aggregator
│
├── modules/
│   ├── core/
│   │   ├── wayang-core-api/               # Core platform APIs
│   │   ├── wayang-core-execution/         # Execution engine
│   │   ├── wayang-core-common/            # Shared utilities
│   │   └── wayang-core-spi/               # Service Provider Interfaces
│   │
│   ├── nodes/
│   │   └── wayang-node-websearch/         # Web Search Node Module
│   │       ├── pom.xml                     # Module aggregator
│   │       │
│   │       ├── websearch-api/              # Public API & Contracts
│   │       │   ├── pom.xml
│   │       │   └── src/main/java/tech/kayys/wayang/node/websearch/
│   │       │       ├── api/
│   │       │       │   ├── SearchRequest.java
│   │       │       │   ├── SearchResponse.java
│   │       │       │   ├── SearchResult.java
│   │       │       │   ├── SearchCapability.java
│   │       │       │   ├── ProviderConfig.java
│   │       │       │   ├── ProviderSearchResult.java
│   │       │       │   ├── EnhancedResult.java
│   │       │       │   ├── ScrapeOptions.java
│   │       │       │   └── ScrapedContent.java
│   │       │       │
│   │       │       ├── spi/
│   │       │       │   ├── SearchProvider.java
│   │       │       │   ├── ResultEnhancer.java
│   │       │       │   ├── HealthStatus.java
│   │       │       │   └── RateLimitStatus.java
│   │       │       │
│   │       │       └── exception/
│   │       │           ├── SearchException.java
│   │       │           ├── RateLimitException.java
│   │       │           ├── ProviderException.java
│   │       │           └── RobotsDisallowedException.java
│   │       │
│   │       ├── websearch-core/              # Core Implementation
│   │       │   ├── pom.xml
│   │       │   └── src/main/java/tech/kayys/wayang/node/websearch/
│   │       │       ├── WebSearchNode.java           # Main node
│   │       │       │
│   │       │       ├── orchestrator/
│   │       │       │   ├── SearchOrchestrator.java
│   │       │       │   ├── ProviderSelector.java
│   │       │       │   ├── ResultAggregator.java
│   │       │       │   ├── ResultDeduplicator.java
│   │       │       │   └── ResultRanker.java
│   │       │       │
│   │       │       ├── cache/
│   │       │       │   ├── SearchCacheManager.java
│   │       │       │   ├── CacheKeyGenerator.java
│   │       │       │   └── CacheEvictionPolicy.java
│   │       │       │
│   │       │       ├── ratelimit/
│   │       │       │   ├── RateLimiter.java
│   │       │       │   ├── TenantQuotaManager.java
│   │       │       │   └── ProviderRateLimiter.java
│   │       │       │
│   │       │       ├── metrics/
│   │       │       │   ├── SearchMetrics.java
│   │       │       │   └── CostTracker.java
│   │       │       │
│   │       │       └── config/
│   │       │           ├── WebSearchConfig.java
│   │       │           └── ProviderConfigLoader.java
│   │       │
│   │       ├── providers/                   # Search Provider Implementations
│   │       │   ├── provider-google/
│   │       │   │   ├── pom.xml
│   │       │   │   └── src/main/java/tech/kayys/wayang/node/websearch/provider/google/
│   │       │   │       ├── GoogleSearchProvider.java
│   │       │   │       ├── GoogleSearchClient.java
│   │       │   │       ├── GoogleResultMapper.java
│   │       │   │       ├── GoogleRateLimiter.java
│   │       │   │       └── model/
│   │       │   │           ├── GoogleApiRequest.java
│   │       │   │           └── GoogleApiResponse.java
│   │       │   │
│   │       │   ├── provider-bing/
│   │       │   │   ├── pom.xml
│   │       │   │   └── src/main/java/.../provider/bing/
│   │       │   │       ├── BingSearchProvider.java
│   │       │   │       ├── BingSearchClient.java
│   │       │   │       └── BingResultMapper.java
│   │       │   │
│   │       │   ├── provider-duckduckgo/
│   │       │   │   ├── pom.xml
│   │       │   │   └── src/main/java/.../provider/duckduckgo/
│   │       │   │       ├── DuckDuckGoProvider.java
│   │       │   │       └── DuckDuckGoClient.java
│   │       │   │
│   │       │   ├── provider-serpapi/
│   │       │   │   └── ...
│   │       │   │
│   │       │   ├── provider-brave/
│   │       │   │   └── ...
│   │       │   │
│   │       │   └── provider-custom/
│   │       │       └── ...
│   │       │
│   │       ├── capabilities/                # Capability Modules
│   │       │   ├── capability-text/
│   │       │   │   └── src/main/java/.../capability/text/
│   │       │   │       └── TextSearchCapability.java
│   │       │   │
│   │       │   ├── capability-image/
│   │       │   │   └── src/main/java/.../capability/image/
│   │       │   │       └── ImageSearchCapability.java
│   │       │   │
│   │       │   ├── capability-video/
│   │       │   ├── capability-news/
│   │       │   ├── capability-academic/
│   │       │   ├── capability-shopping/
│   │       │   ├── capability-maps/
│   │       │   ├── capability-code/
│   │       │   │
│   │       │   ├── capability-scrape/
│   │       │   │   └── src/main/java/.../capability/scrape/
│   │       │   │       ├── WebScrapingCapability.java
│   │       │   │       ├── ContentExtractor.java
│   │       │   │       ├── RobotsTxtChecker.java
│   │       │   │       └── HttpClient.java
│   │       │   │
│   │       │   └── capability-semantic/
│   │       │       └── src/main/java/.../capability/semantic/
│   │       │           ├── SemanticSearchCapability.java
│   │       │           └── VectorSearchIntegration.java
│   │       │
│   │       ├── enhancers/                   # Enhancement Pipeline
│   │       │   ├── enhancer-core/
│   │       │   │   └── src/main/java/.../enhancer/
│   │       │   │       ├── EnhancementPipeline.java
│   │       │   │       └── ResultEnhancer.java
│   │       │   │
│   │       │   ├── enhancer-extract/
│   │       │   │   └── src/main/java/.../enhancer/extract/
│   │       │   │       ├── ContentExtractionEnhancer.java
│   │       │   │       └── ReadabilityExtractor.java
│   │       │   │
│   │       │   ├── enhancer-entity/
│   │       │   │   └── src/main/java/.../enhancer/entity/
│   │       │   │       ├── EntityExtractionEnhancer.java
│   │       │   │       └── NERProcessor.java
│   │       │   │
│   │       │   ├── enhancer-summarize/
│   │       │   │   └── src/main/java/.../enhancer/summarize/
│   │       │   │       ├── SummarizationEnhancer.java
│   │       │   │       └── LLMSummarizer.java
│   │       │   │
│   │       │   ├── enhancer-translate/
│   │       │   │   └── src/main/java/.../enhancer/translate/
│   │       │   │       └── TranslationEnhancer.java
│   │       │   │
│   │       │   ├── enhancer-screenshot/
│   │       │   │   └── src/main/java/.../enhancer/screenshot/
│   │       │   │       ├── ScreenshotEnhancer.java
│   │       │   │       └── PlaywrightCapture.java
│   │       │   │
│   │       │   └── enhancer-quality/
│   │       │       └── src/main/java/.../enhancer/quality/
│   │       │           ├── QualityScoringEnhancer.java
│   │       │           └── RelevanceScorer.java
│   │       │
│   │       ├── websearch-runtime/           # Minimal Runtime for Standalone
│   │       │   ├── pom.xml
│   │       │   └── src/main/java/.../runtime/
│   │       │       ├── StandaloneSearchRuntime.java
│   │       │       ├── LiteCache.java
│   │       │       ├── LiteRateLimiter.java
│   │       │       └── LiteAudit.java
│   │       │
│   │       └── websearch-deployment/         # Deployment Configs
│   │           ├── kubernetes/
│   │           │   ├── deployment.yaml
│   │           │   ├── service.yaml
│   │           │   ├── configmap.yaml
│   │           │   └── secret.yaml
│   │           │
│   │           ├── docker/
│   │           │   ├── Dockerfile.jvm
│   │           │   ├── Dockerfile.native
│   │           │   └── docker-compose.yml
│   │           │
│   │           └── helm/
│   │               └── websearch/
│   │                   ├── Chart.yaml
│   │                   ├── values.yaml
│   │                   └── templates/
│   │
│   └── platform-services/
│       ├── designer-service/
│       ├── orchestrator-service/
│       ├── codegen-service/
│       └── ...
│
├── standalone-runtime/
│   └── agent-runtime-sdk/
│       ├── pom.xml
│       └── nodes/
│           └── websearch/
│               └── (minimal subset of above)
│
└── schemas/
    └── nodes/
        └── web-search-node.yaml
```

---

## Technology Stack

### Core Framework
- **Quarkus 3.6+**: Main framework
  - `quarkus-arc`: CDI/Dependency Injection
  - `quarkus-rest-client-reactive`: HTTP clients
  - `quarkus-rest-client-reactive-jackson`: JSON processing
  - `quarkus-cache`: Caching abstraction
  - `quarkus-scheduler`: Job scheduling
  - `quarkus-smallrye-fault-tolerance`: Circuit breakers, retries

### Reactive & Resilience
- **Mutiny**: Reactive programming
- **SmallRye Fault Tolerance**: Resilience patterns
  - Circuit Breaker
  - Retry
  - Timeout
  - Bulkhead

### Multi-Tenancy
- **Hibernate ORM with Panache**: Data access
- **PostgreSQL**: Primary database
- **Tenant Discriminator**: Row-level multi-tenancy
- **Tenant Context Propagation**: Request-scoped tenant tracking

### Observability
- **OpenTelemetry**: Distributed tracing
- **Micrometer + Prometheus**: Metrics
- **Quarkus Logging JSON**: Structured logging
- **Jaeger/Tempo**: Trace backend

### Security
- **SmallRye JWT**: Token-based auth
- **Quarkus Security**: RBAC/ABAC
- **HashiCorp Vault**: Secrets management
- **TLS/mTLS**: Transport security

### Web Scraping
- **Jsoup 1.17.2**: HTML parsing
- **Playwright (optional)**: JavaScript rendering
- **Apache Tika (optional)**: Document parsing

### NLP/Enhancement (Optional)
- **Stanford CoreNLP**: NER
- **SpaCy via Python subprocess**: Advanced NLP

### Testing
- **JUnit 5**: Unit testing
- **Quarkus Test**: Integration testing
- **TestContainers**: Container-based tests
- **RestAssured**: API testing
- **WireMock**: HTTP mocking

### Build & Deployment
- **Maven 3.9+**: Build tool
- **Docker**: Containerization
- **Kubernetes**: Orchestration
- **Helm 3**: Package manager
- **GraalVM Native Image**: Native compilation

---

## Core Implementation Files

### 1. Root POM (`pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>tech.kayys.wayang</groupId>
    <artifactId>wayang-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Wayang :: Platform</name>
    <description>Low-Code AI Agent Workflow Builder</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Versions -->
        <quarkus.platform.version>3.6.4</quarkus.platform.version>
        <mutiny.version>2.5.1</mutiny.version>
        <jsoup.version>1.17.2</jsoup.version>
        <testcontainers.version>1.19.3</testcontainers.version>
        
        <!-- Plugin Versions -->
        <maven.compiler.plugin.version>3.12.1</maven.compiler.plugin.version>
        <maven.surefire.plugin.version>3.2.3</maven.surefire.plugin.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Quarkus BOM -->
            <dependency>
                <groupId>io.quarkus.platform</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>${quarkus.platform.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Wayang Core -->
            <dependency>
                <groupId>tech.kayys.wayang</groupId>
                <artifactId>wayang-core-api</artifactId>
                <version>${project.version}</version>
            </dependency>

            <!-- External -->
            <dependency>
                <groupId>org.jsoup</groupId>
                <artifactId>jsoup</artifactId>
                <version>${jsoup.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <modules>
        <module>modules/core</module>
        <module>modules/nodes/wayang-node-websearch</module>
        <module>modules/platform-services</module>
        <module>standalone-runtime</module>
    </modules>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>io.quarkus</groupId>
                    <artifactId>quarkus-maven-plugin</artifactId>
                    <version>${quarkus.platform.version}</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>${maven.compiler.plugin.version}</version>
                    <configuration>
                        <parameters>true</parameters>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### 2. Web Search Module POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>tech.kayys.wayang</groupId>
        <artifactId>wayang-nodes</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>wayang-node-websearch</artifactId>
    <packaging>pom</packaging>

    <name>Wayang :: Nodes :: WebSearch</name>

    <modules>
        <module>websearch-api</module>
        <module>websearch-core</module>
        <module>providers/provider-google</module>
        <module>providers/provider-bing</module>
        <module>providers/provider-duckduckgo</module>
        <module>capabilities/capability-scrape</module>
        <module>enhancers/enhancer-core</module>
        <module>enhancers/enhancer-extract</module>
        <module>websearch-runtime</module>
    </modules>
</project>
```

### 3. Application Configuration

**File**: `websearch-core/src/main/resources/application.properties`

```properties
# Quarkus Configuration
quarkus.application.name=wayang-websearch-node
quarkus.application.version=${project.version}

# HTTP Configuration
quarkus.http.port=8080
quarkus.http.test-port=8081

# Multi-Tenancy
wayang.multi-tenancy.enabled=true
wayang.multi-tenancy.strategy=discriminator
wayang.multi-tenancy.default-tenant=default

# Datasource (Multi-Tenant)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:postgres}
quarkus.datasource.password=${DB_PASS:postgres}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/wayang}

# Hibernate
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.multitenant=DISCRIMINATOR
quarkus.hibernate-orm.log.sql=false

# REST Client
quarkus.rest-client.logging.scope=request-response
quarkus.rest-client.logging.body-limit=1000

# Cache
quarkus.cache.type=caffeine
quarkus.cache.caffeine."search-cache".initial-capacity=100
quarkus.cache.caffeine."search-cache".maximum-size=10000
quarkus.cache.caffeine."search-cache".expire-after-write=1H

# Metrics
quarkus.micrometer.enabled=true
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.export.prometheus.path=/metrics

# OpenTelemetry
quarkus.otel.enabled=true
quarkus.otel.traces.enabled=true
quarkus.otel.exporter.otlp.traces.endpoint=${OTEL_ENDPOINT:http://localhost:4317}

# Logging
quarkus.log.level=INFO
quarkus.log.category."tech.kayys.wayang".level=DEBUG
quarkus.log.console.json=true

# Fault Tolerance
quarkus.fault-tolerance.enabled=true

# Web Search Configuration
websearch.enable-multi-provider=false
websearch.parallel-providers=false
websearch.default-max-results=10
websearch.default-timeout=30s

# Provider: Google
websearch.provider.google.enabled=${GOOGLE_SEARCH_ENABLED:true}
websearch.provider.google.api-key=${GOOGLE_SEARCH_API_KEY:}
websearch.provider.google.search-engine-id=${GOOGLE_SEARCH_ENGINE_ID:}
websearch.provider.google.rate-limit=100
websearch.provider.google.rate-limit-period=1d
websearch.provider.google.timeout=15s
websearch.provider.google.priority=100

# Provider: Bing
websearch.provider.bing.enabled=${BING_SEARCH_ENABLED:true}
websearch.provider.bing.api-key=${BING_SEARCH_API_KEY:}
websearch.provider.bing.rate-limit=1000
websearch.provider.bing.rate-limit-period=1mo
websearch.provider.bing.timeout=15s
websearch.provider.bing.priority=90

# Provider: DuckDuckGo
websearch.provider.duckduckgo.enabled=${DUCKDUCKGO_ENABLED:true}
websearch.provider.duckduckgo.rate-limit=unlimited
websearch.provider.duckduckgo.timeout=20s
websearch.provider.duckduckgo.priority=70

# Scraping
websearch.scrape.respect-robots-txt=true
websearch.scrape.user-agent=WayangBot/1.0
websearch.scrape.timeout=30s
websearch.scrape.max-content-size=5MB

# Rate Limiting
websearch.ratelimit.enabled=true
websearch.ratelimit.per-tenant=true
websearch.ratelimit.default-limit=100
websearch.ratelimit.default-period=1h

# Circuit Breaker
websearch.circuit-breaker.enabled=true
websearch.circuit-breaker.failure-threshold=5
websearch.circuit-breaker.delay=60s

# Retry
websearch.retry.enabled=true
websearch.retry.max-attempts=3
websearch.retry.backoff=exponential

# Audit
websearch.audit.enabled=true
websearch.audit.log-queries=true
websearch.audit.retention-days=90
```

---

## Multi-Tenancy Implementation

### Tenant Context

```java
package tech.kayys.wayang.core.tenant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.RequestScoped;

/**
 * Tenant Context - Request-scoped tenant information
 */
@RequestScoped
@RegisterForReflection
public class TenantContext {
    
    private String tenantId;
    private TenantConfiguration config;
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
        // Load tenant config from database
        this.config = loadTenantConfig(tenantId);
    }
    
    public String getTenantId() {
        return tenantId != null ? tenantId : "default";
    }
    
    public TenantConfiguration getConfig() {
        return config;
    }
    
    private TenantConfiguration loadTenantConfig(String tenantId) {
        // Load from DB or cache
        return TenantConfiguration.getDefault();
    }
}
```

### Tenant Interceptor

```java
package tech.kayys.wayang.core.tenant;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@MultiTenant
@Interceptor
public class TenantInterceptor {
    
    @Inject
    TenantContext tenantContext;
    
    @Inject
    TenantResolver tenantResolver;
    
    @AroundInvoke
    public Object setupTenant(InvocationContext ctx) throws Exception {
        String tenantId = tenantResolver.resolveTenantId();
        tenantContext.setTenantId(tenantId);
        
        try {
            return ctx.proceed();
        } finally {
            // Cleanup if needed
        }
    }
}
```

### Tenant-Aware Rate Limiter

```java
package tech.kayys.wayang.node.websearch.ratelimit;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.core.tenant.TenantContext;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TenantRateLimiter {
    
    @Inject
    TenantContext tenantContext;
    
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String operation) {
        String tenantId = tenantContext.getTenantId();
        String key = tenantId + ":" + operation;
        
        RateLimitBucket bucket = buckets.computeIfAbsent(
            key,
            k -> new RateLimitBucket(getTenantQuota(tenantId, operation))
        );
        
        return bucket.tryConsume();
    }
    
    private Quota getTenantQuota(String tenantId, String operation) {
        // Load from tenant configuration
        return new Quota(100, Duration.ofHours(1));
    }
}
```

---

## Error Handling & Audit

### Error Handling Implementation

```java
package tech.kayys.wayang.node.websearch;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.core.execution.ErrorPayload;
import tech.kayys.wayang.core.execution.ExecutionResult;

@ApplicationScoped
public class WebSearchNode extends IntegrationNode {
    
    @Override
    protected Uni<ExecutionResult> executeIntegration(NodeContext context) {
        SearchRequest request = parseSearchRequest(context);
        
        return orchestrator.search(request, context)
            .onItem().transform(this::toExecutionResult)
            
            // Handle specific error types
            .onFailure(RateLimitException.class).recoverWithItem(error -> {
                ErrorPayload payload = ErrorPayload.builder()
                    .type("RateLimitError")
                    .message("Search quota exceeded")
                    .retryable(true)
                    .details(Map.of(
                        "provider", error.getProvider(),
                        "resetTime", error.getResetTime()
                    ))
                    .build();
                    
                return ExecutionResult.error(payload);
            })
            
            // Provider fallback
            .onFailure(ProviderException.class).recoverWithUni(error -> {
                if (request.hasMoreProviders()) {
                    return orchestrator.search(
                        request.withNextProvider(), 
                        context
                    );
                }
                return Uni.createFrom().failure(error);
            })
            
            // Generic error
            .onFailure().recoverWithItem(error -> 
                ExecutionResult.error(
                    ErrorPayload.from(error, descriptor.getId(), context)
                )
            );
    }
}
```

### Audit Integration

```java
package tech.kayys.wayang.node.websearch.audit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.core.audit.AuditService;
import tech.kayys.wayang.core.audit.AuditEntry;

@ApplicationScoped
public class SearchAuditLogger {
    
    @Inject
    AuditService auditService;
    
    public void logSearch(SearchRequest request, SearchResponse response, NodeContext context) {
        AuditEntry entry = AuditEntry.builder()
            .event("WEB_SEARCH_EXECUTED")
            .level(AuditLevel.INFO)
            .actor(ActorInfo.system())
            .resource(ResourceInfo.builder()
                .type("web-search")
                .id(context.getNodeId())
                .build())
            .metadata(Map.of(
                "query", sanitize(request.getQuery()),
                "searchType", request.getSearchType(),
                "provider", response.getProviderUsed(),
                "resultCount", response.getTotalResults(),
                "cacheHit", response.isCacheHit(),
                "duration", response.getDuration()
            ))
            .tenantId(context.getTenantId())
            .workflowId(context.getWorkflowId())
            .runId(context.getRunId())
            .timestamp(Instant.now())
            .build();
        
        auditService.log(entry);
    }
    
    private String sanitize(String query) {
        // Remove PII if needed
        return query;
    }
}
```

---

## Testing Strategy

### Unit Tests

```java
package tech.kayys.wayang.node.websearch;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WebSearchNodeTest {
    
    @Inject
    WebSearchNode searchNode;
    
    @InjectMock
    SearchOrchestrator orchestrator;
    
    @Test
    void testSearchExecution() {
        // Given
        NodeContext context = createTestContext();
        SearchResponse mockResponse = createMockResponse();
        
        when(orchestrator.search(any(), any()))
            .thenReturn(Uni.createFrom().item(mockResponse));
        
        // When
        ExecutionResult result = searchNode.execute(context)
            .await().indefinitely();
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(10, result.getOutput("totalResults"));
    }
}
```

### Integration Tests

```java
@QuarkusTest
@TestProfile(WebSearchTestProfile.class)
@QuarkusTestResource(PostgresResource.class)
class WebSearchIntegrationTest {
    
    @Test
    void testGoogleProvider() {
        // Test with real Google API (if key available)
    }
}
```

---

## CI/CD Pipeline

### GitHub Actions Example

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: mvn clean verify -Pnative
      
      - name: Build Docker Image
        run: docker build -f docker/Dockerfile.native -t wayang/websearch:${{ github.sha }} .
      
      - name: Push to Registry
        run: docker push wayang/websearch:${{ github.sha }}
```

---

## Next Steps

1. **Implement Remaining Providers**: Bing, SerpAPI, Brave
2. **Add Enhancement Modules**: NER, Summarization, Translation
3. **Create Helm Charts**: For Kubernetes deployment
4. **Build Monitoring Dashboards**: Grafana dashboards
5. **Write Documentation**: API docs, user guides
6. **Performance Testing**: Load tests, benchmarking
7. **Security Audit**: Penetration testing, code analysis

---

**End of Implementation Guide**

For questions or contributions, contact: team@kayys.tech
