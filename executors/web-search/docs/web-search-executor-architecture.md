# Web Search Executor - Modular Architecture

## Overview

The Web Search Executor is a pluggable, multi-provider node within the Wayang AI Agent Workflow Builder that enables intelligent web searching, scraping, and content extraction. Built with Quarkus and designed for both platform and standalone runtime deployment.

---

## Architecture Principles

1. **Modular & Extensible**: Each search provider is a separate SPI implementation
2. **Multi-Tenancy**: Full tenant isolation with quota management
3. **Provider Agnostic**: Support multiple search engines via unified interface
4. **Observable**: Complete tracing, metrics, and audit trails
5. **Resilient**: Circuit breakers, retries, fallbacks
6. **Portable**: Same code runs in platform and standalone agents

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Web Search Executor Node                     │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Search Orchestration Layer                     │  │
│  │  - Request Router                                        │  │
│  │  - Multi-Provider Selector                              │  │
│  │  - Result Aggregator & Deduplicator                     │  │
│  │  - Ranking & Re-ranking Engine                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Provider SPI Layer (Pluggable)                 │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │  Google Search │ Bing │ DuckDuckGo │ SerpAPI │ Custom    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Capability Modules (Composable)                │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │  • Text Search        • Image Search                     │  │
│  │  • Video Search       • News Search                      │  │
│  │  • Academic Search    • Local/Map Search                 │  │
│  │  • Shopping Search    • Code Search                      │  │
│  │  • Web Scraping       • PDF Extraction                   │  │
│  │  • Semantic Search    • Hybrid Search                    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Enhancement Pipeline                           │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │  • Content Extraction    • Metadata Enrichment          │  │
│  │  • Entity Recognition    • Summarization                │  │
│  │  • Translation          • Quality Scoring                │  │
│  │  • Screenshot Capture    • Content Cleaning             │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Infrastructure Layer                           │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │  • Rate Limiter         • Cache Manager                  │  │
│  │  • Retry Handler        • Circuit Breaker                │  │
│  │  • Guardrails          • Cost Tracker                    │  │
│  │  • Audit Logger        • Telemetry                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Module Structure

### Project Layout

```
wayang-platform/
├── modules/
│   ├── core/
│   │   └── wayang-core/                    # Core abstractions
│   │       ├── node-api/                   # Node SPI
│   │       ├── execution-api/              # Execution contracts
│   │       └── common/                     # Shared utilities
│   │
│   ├── nodes/
│   │   └── wayang-node-websearch/         # Web Search Node
│   │       ├── websearch-api/             # Public API & SPI
│   │       ├── websearch-core/            # Core implementation
│   │       ├── websearch-runtime/         # Runtime module (minimal)
│   │       │
│   │       ├── providers/                 # Search Provider SPIs
│   │       │   ├── provider-google/
│   │       │   ├── provider-bing/
│   │       │   ├── provider-duckduckgo/
│   │       │   ├── provider-serpapi/
│   │       │   ├── provider-brave/
│   │       │   └── provider-custom/
│   │       │
│   │       ├── capabilities/              # Modular Capabilities
│   │       │   ├── capability-text/
│   │       │   ├── capability-image/
│   │       │   ├── capability-video/
│   │       │   ├── capability-news/
│   │       │   ├── capability-academic/
│   │       │   ├── capability-shopping/
│   │       │   ├── capability-maps/
│   │       │   ├── capability-scrape/
│   │       │   └── capability-semantic/
│   │       │
│   │       ├── enhancers/                 # Enhancement Pipeline
│   │       │   ├── enhancer-extract/      # Content extraction
│   │       │   ├── enhancer-entity/       # NER
│   │       │   ├── enhancer-summarize/    # Summarization
│   │       │   ├── enhancer-translate/    # Translation
│   │       │   ├── enhancer-screenshot/   # Screenshot capture
│   │       │   └── enhancer-quality/      # Quality scoring
│   │       │
│   │       └── infrastructure/
│   │           ├── cache/                 # Result caching
│   │           ├── ratelimit/             # Rate limiting
│   │           ├── retry/                 # Retry logic
│   │           └── circuit-breaker/       # Fault tolerance
│   │
│   └── platform-services/
│       ├── designer-service/
│       ├── orchestrator-service/
│       └── codegen-service/
│
└── standalone-runtime/
    └── agent-runtime-sdk/                 # Minimal runtime for agents
        └── nodes/
            └── websearch/                 # Only what's needed
```

---

## Core Components

### 1. Web Search Node (Main Entry Point)

**File**: `websearch-core/src/main/java/tech/kayys/wayang/node/websearch/WebSearchNode.java`

```java
package tech.kayys.wayang.node.websearch;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.core.node.IntegrationNode;
import tech.kayys.wayang.core.execution.NodeContext;
import tech.kayys.wayang.core.execution.ExecutionResult;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.api.SearchResponse;
import tech.kayys.wayang.node.websearch.orchestrator.SearchOrchestrator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Web Search Node - Main entry point for web search capabilities
 * 
 * Supports multiple search types through modular capabilities:
 * - Text Search
 * - Image Search
 * - Video Search
 * - News Search
 * - Academic/Scholar Search
 * - Shopping Search
 * - Local/Maps Search
 * - Web Scraping
 * - Semantic/Hybrid Search
 * 
 * Features:
 * - Multi-provider support (Google, Bing, DuckDuckGo, SerpAPI, etc.)
 * - Automatic provider fallback
 * - Result deduplication and ranking
 * - Content extraction and enhancement
 * - Rate limiting and caching
 * - Full observability and audit trails
 * 
 * @see IntegrationNode
 * @see SearchOrchestrator
 */
@ApplicationScoped
public class WebSearchNode extends IntegrationNode {

    @Inject
    SearchOrchestrator orchestrator;

    @Override
    protected Uni<ExecutionResult> executeIntegration(NodeContext context) {
        
        // 1. Parse input into SearchRequest
        SearchRequest request = parseSearchRequest(context);
        
        // 2. Validate request against guardrails
        return validateRequest(request, context)
            .onItem().transformToUni(validationResult -> {
                if (!validationResult.isValid()) {
                    return Uni.createFrom().item(
                        ExecutionResult.failed(validationResult.getMessage())
                    );
                }
                
                // 3. Execute search via orchestrator
                return orchestrator.search(request, context)
                    .onItem().transform(this::toExecutionResult)
                    .onFailure().recoverWithItem(error -> 
                        ExecutionResult.error(createErrorPayload(error, context))
                    );
            });
    }

    private SearchRequest parseSearchRequest(NodeContext context) {
        return SearchRequest.builder()
            .query(context.getInput("query", String.class))
            .searchType(context.getInput("searchType", "text"))
            .maxResults(context.getInput("maxResults", 10))
            .providers(context.getInput("providers", List.class))
            .enableCache(context.getInput("enableCache", true))
            .enableEnhancements(context.getInput("enableEnhancements", false))
            .filters(context.getInput("filters", Map.class))
            .locale(context.getInput("locale", "en"))
            .safeSearch(context.getInput("safeSearch", true))
            .tenantId(context.getTenantId())
            .build();
    }

    private ExecutionResult toExecutionResult(SearchResponse response) {
        return ExecutionResult.builder()
            .success(true)
            .output("results", response.getResults())
            .output("metadata", response.getMetadata())
            .output("providerUsed", response.getProviderUsed())
            .output("totalResults", response.getTotalResults())
            .output("searchDuration", response.getDuration())
            .build();
    }
}
```

---

### 2. Search Provider SPI

**File**: `websearch-api/src/main/java/tech/kayys/wayang/node/websearch/spi/SearchProvider.java`

```java
package tech.kayys.wayang.node.websearch.spi;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.node.websearch.api.*;

import java.util.Set;

/**
 * Search Provider SPI - Implement this to add new search providers
 * 
 * Implementations register via ServiceLoader or CDI discovery.
 * 
 * Example providers:
 * - GoogleSearchProvider
 * - BingSearchProvider
 * - DuckDuckGoProvider
 * - SerpApiProvider
 * - BraveSearchProvider
 * 
 * Each provider can support different capabilities (text, image, news, etc.)
 */
public interface SearchProvider {

    /**
     * Unique provider identifier (e.g., "google", "bing", "duckduckgo")
     */
    String getProviderId();

    /**
     * Human-readable provider name
     */
    String getProviderName();

    /**
     * What capabilities does this provider support?
     * 
     * @return Set of supported SearchCapability types
     */
    Set<SearchCapability> getSupportedCapabilities();

    /**
     * Execute a search request
     * 
     * @param request The search request with query, filters, etc.
     * @param config Provider-specific configuration
     * @return Search results wrapped in Uni for reactive execution
     */
    Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config);

    /**
     * Check if provider is healthy and available
     */
    Uni<HealthStatus> checkHealth();

    /**
     * Get current rate limit status
     */
    RateLimitStatus getRateLimitStatus(String tenantId);

    /**
     * Provider priority (higher = preferred when multiple providers available)
     * Default: 50
     */
    default int getPriority() {
        return 50;
    }

    /**
     * Is this provider enabled globally?
     */
    default boolean isEnabled() {
        return true;
    }
}
```

**File**: `websearch-api/src/main/java/tech/kayys/wayang/node/websearch/api/SearchCapability.java`

```java
package tech.kayys.wayang.node.websearch.api;

/**
 * Defines the types of search capabilities a provider can support
 */
public enum SearchCapability {
    TEXT_SEARCH,          // General web search
    IMAGE_SEARCH,         // Image-specific search
    VIDEO_SEARCH,         // Video search
    NEWS_SEARCH,          // News articles
    ACADEMIC_SEARCH,      // Google Scholar, Semantic Scholar
    SHOPPING_SEARCH,      // Product search
    LOCAL_SEARCH,         // Maps, local businesses
    CODE_SEARCH,          // GitHub, code repositories
    
    // Advanced capabilities
    WEB_SCRAPING,         // Full page content extraction
    SEMANTIC_SEARCH,      // Vector/embedding-based search
    HYBRID_SEARCH,        // Combines keyword + semantic
    
    // Content features
    CONTENT_EXTRACTION,   // Clean text extraction from URLs
    SCREENSHOT_CAPTURE,   // Visual page capture
    PDF_EXTRACTION,       // Extract text from PDFs
    ENTITY_EXTRACTION,    // NER on results
    
    // Metadata
    PREVIEW_GENERATION,   // Generate rich previews
    QUALITY_SCORING       // Score result relevance/quality
}
```

---

### 3. Search Orchestrator (Multi-Provider Coordination)

**File**: `websearch-core/src/main/java/tech/kayys/wayang/node/websearch/orchestrator/SearchOrchestrator.java`

```java
package tech.kayys.wayang.node.websearch.orchestrator;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.core.execution.NodeContext;
import tech.kayys.wayang.node.websearch.api.*;
import tech.kayys.wayang.node.websearch.spi.SearchProvider;
import tech.kayys.wayang.node.websearch.cache.SearchCacheManager;
import tech.kayys.wayang.node.websearch.enhancer.EnhancementPipeline;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Search Orchestrator - Coordinates multiple search providers
 * 
 * Responsibilities:
 * - Select best provider(s) for request
 * - Execute searches (sequential or parallel)
 * - Aggregate and deduplicate results
 * - Apply enhancements
 * - Manage caching
 * - Handle fallbacks
 */
@ApplicationScoped
public class SearchOrchestrator {

    @Inject
    Instance<SearchProvider> providers;

    @Inject
    SearchCacheManager cacheManager;

    @Inject
    EnhancementPipeline enhancementPipeline;

    @Inject
    ProviderSelector providerSelector;

    @Inject
    ResultAggregator resultAggregator;

    @ConfigProperty(name = "websearch.enable-multi-provider", defaultValue = "false")
    boolean enableMultiProvider;

    @ConfigProperty(name = "websearch.parallel-providers", defaultValue = "false")
    boolean parallelProviders;

    public Uni<SearchResponse> search(SearchRequest request, NodeContext context) {
        
        // 1. Check cache first
        if (request.isEnableCache()) {
            return cacheManager.get(request)
                .onItem().ifNotNull().transform(Uni::createFrom)
                .orElseGet(() -> executeSearch(request, context));
        }
        
        return executeSearch(request, context);
    }

    private Uni<SearchResponse> executeSearch(SearchRequest request, NodeContext context) {
        
        // 1. Select provider(s) based on capability, quota, health
        List<SearchProvider> selectedProviders = providerSelector.selectProviders(
            request, 
            getAvailableProviders(),
            enableMultiProvider
        );

        if (selectedProviders.isEmpty()) {
            return Uni.createFrom().failure(
                new IllegalStateException("No suitable search provider available")
            );
        }

        // 2. Execute search (sequential or parallel)
        Uni<List<ProviderSearchResult>> searchResults = parallelProviders
            ? executeParallel(selectedProviders, request)
            : executeSequential(selectedProviders, request);

        // 3. Aggregate, deduplicate, rank results
        return searchResults
            .onItem().transformToUni(results -> 
                resultAggregator.aggregate(results, request)
            )
            // 4. Apply enhancements if requested
            .onItem().transformToUni(aggregated -> {
                if (request.isEnableEnhancements()) {
                    return enhancementPipeline.enhance(aggregated, request, context);
                }
                return Uni.createFrom().item(aggregated);
            })
            // 5. Cache result
            .onItem().invoke(result -> {
                if (request.isEnableCache()) {
                    cacheManager.put(request, result);
                }
            });
    }

    private Uni<List<ProviderSearchResult>> executeSequential(
        List<SearchProvider> providers, 
        SearchRequest request
    ) {
        return Uni.createFrom().item(providers.get(0))
            .onItem().transformToUni(provider -> 
                provider.search(request, getProviderConfig(provider))
                    .onItem().transform(List::of)
                    .onFailure().recoverWithUni(error -> {
                        // Try next provider on failure
                        if (providers.size() > 1) {
                            return executeSequential(
                                providers.subList(1, providers.size()), 
                                request
                            );
                        }
                        return Uni.createFrom().failure(error);
                    })
            );
    }

    private Uni<List<ProviderSearchResult>> executeParallel(
        List<SearchProvider> providers, 
        SearchRequest request
    ) {
        List<Uni<ProviderSearchResult>> searches = providers.stream()
            .map(provider -> provider.search(request, getProviderConfig(provider))
                .onFailure().recoverWithNull()) // Don't fail entire search
            .collect(Collectors.toList());

        return Uni.combine().all().unis(searches).combinedWith(results -> 
            results.stream()
                .filter(Objects::nonNull)
                .map(r -> (ProviderSearchResult) r)
                .collect(Collectors.toList())
        );
    }

    private List<SearchProvider> getAvailableProviders() {
        return providers.stream()
            .filter(SearchProvider::isEnabled)
            .collect(Collectors.toList());
    }

    private ProviderConfig getProviderConfig(SearchProvider provider) {
        // Load config from application.properties or tenant-specific settings
        return ProviderConfig.forProvider(provider.getProviderId());
    }
}
```

---

### 4. Provider Implementation Example: Google Search

**File**: `providers/provider-google/src/main/java/tech/kayys/wayang/node/websearch/provider/google/GoogleSearchProvider.java`

```java
package tech.kayys.wayang.node.websearch.provider.google;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.node.websearch.spi.SearchProvider;
import tech.kayys.wayang.node.websearch.api.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Set;

/**
 * Google Custom Search API Provider
 * 
 * Supports: Text, Image, News search
 * Requires: Google Custom Search API key + Search Engine ID
 * 
 * Configuration:
 * - websearch.provider.google.api-key
 * - websearch.provider.google.search-engine-id
 * - websearch.provider.google.rate-limit (requests/day)
 */
@ApplicationScoped
public class GoogleSearchProvider implements SearchProvider {

    @Inject
    @RestClient
    GoogleSearchClient client;

    @Inject
    GoogleRateLimiter rateLimiter;

    @Inject
    GoogleResultMapper resultMapper;

    @Override
    public String getProviderId() {
        return "google";
    }

    @Override
    public String getProviderName() {
        return "Google Custom Search";
    }

    @Override
    public Set<SearchCapability> getSupportedCapabilities() {
        return Set.of(
            SearchCapability.TEXT_SEARCH,
            SearchCapability.IMAGE_SEARCH,
            SearchCapability.NEWS_SEARCH
        );
    }

    @Override
    public Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config) {
        
        // 1. Check rate limit
        if (!rateLimiter.allowRequest(request.getTenantId())) {
            return Uni.createFrom().failure(
                new RateLimitException("Google search quota exceeded")
            );
        }

        // 2. Build Google API request
        GoogleApiRequest apiRequest = buildApiRequest(request, config);

        // 3. Execute HTTP call
        return client.search(apiRequest)
            .onItem().transform(response -> 
                resultMapper.mapToProviderResult(response, request)
            )
            .onItem().invoke(() -> 
                rateLimiter.recordRequest(request.getTenantId())
            );
    }

    @Override
    public Uni<HealthStatus> checkHealth() {
        // Ping Google API to verify connectivity and credentials
        return client.ping()
            .onItem().transform(pong -> HealthStatus.healthy())
            .onFailure().recoverWithItem(
                error -> HealthStatus.unhealthy(error.getMessage())
            );
    }

    @Override
    public RateLimitStatus getRateLimitStatus(String tenantId) {
        return rateLimiter.getStatus(tenantId);
    }

    @Override
    public int getPriority() {
        return 100; // Highest priority (most reliable)
    }

    private GoogleApiRequest buildApiRequest(SearchRequest request, ProviderConfig config) {
        return GoogleApiRequest.builder()
            .query(request.getQuery())
            .apiKey(config.getApiKey())
            .searchEngineId(config.getSearchEngineId())
            .num(Math.min(request.getMaxResults(), 10))
            .safe(request.isSafeSearch() ? "active" : "off")
            .searchType(mapSearchType(request.getSearchType()))
            .build();
    }

    private String mapSearchType(String type) {
        return switch (type) {
            case "image" -> "image";
            case "news" -> ""; // Use date restriction
            default -> "";
        };
    }
}
```

---

### 5. Capability Module Example: Web Scraping

**File**: `capabilities/capability-scrape/src/main/java/tech/kayys/wayang/node/websearch/capability/scrape/WebScrapingCapability.java`

```java
package tech.kayys.wayang.node.websearch.capability.scrape;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.node.websearch.api.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.Duration;

/**
 * Web Scraping Capability
 * 
 * Extracts full content from web pages
 * Features:
 * - HTML parsing with Jsoup
 * - Content cleaning and extraction
 * - Metadata extraction (title, description, author, publish date)
 * - Readability/article extraction
 * - Respect robots.txt
 * - User-agent rotation
 * - JavaScript rendering (optional with Playwright)
 */
@ApplicationScoped
public class WebScrapingCapability {

    @Inject
    HttpClient httpClient;

    @Inject
    ContentExtractor contentExtractor;

    @Inject
    RobotsTxtChecker robotsChecker;

    public Uni<ScrapedContent> scrape(String url, ScrapeOptions options) {
        
        // 1. Check robots.txt if configured
        if (options.isRespectRobotsTxt()) {
            return robotsChecker.isAllowed(url)
                .onItem().transformToUni(allowed -> {
                    if (!allowed) {
                        return Uni.createFrom().failure(
                            new RobotsDisallowedException(url)
                        );
                    }
                    return performScrape(url, options);
                });
        }

        return performScrape(url, options);
    }

    private Uni<ScrapedContent> performScrape(String url, ScrapeOptions options) {
        
        return httpClient.fetchHtml(url, options.getTimeout())
            .onItem().transform(html -> {
                Document doc = Jsoup.parse(html, url);
                
                return ScrapedContent.builder()
                    .url(url)
                    .title(extractTitle(doc))
                    .mainContent(contentExtractor.extractMainContent(doc))
                    .metadata(extractMetadata(doc))
                    .links(extractLinks(doc, options))
                    .images(extractImages(doc, options))
                    .html(options.isIncludeHtml() ? html : null)
                    .scrapedAt(Instant.now())
                    .build();
            });
    }

    private String extractTitle(Document doc) {
        return doc.title();
    }

    private Map<String, Object> extractMetadata(Document doc) {
        Map<String, Object> metadata = new HashMap<>();
        
        // Open Graph tags
        metadata.put("og:title", getMetaContent(doc, "og:title"));
        metadata.put("og:description", getMetaContent(doc, "og:description"));
        metadata.put("og:image", getMetaContent(doc, "og:image"));
        
        // Twitter Card tags
        metadata.put("twitter:title", getMetaContent(doc, "twitter:title"));
        metadata.put("twitter:description", getMetaContent(doc, "twitter:description"));
        
        // Standard meta tags
        metadata.put("description", getMetaContent(doc, "description"));
        metadata.put("keywords", getMetaContent(doc, "keywords"));
        metadata.put("author", getMetaContent(doc, "author"));
        
        return metadata;
    }

    private String getMetaContent(Document doc, String property) {
        var element = doc.select("meta[property=" + property + "]").first();
        if (element == null) {
            element = doc.select("meta[name=" + property + "]").first();
        }
        return element != null ? element.attr("content") : null;
    }
}
```

---

### 6. Enhancement Pipeline

**File**: `websearch-core/src/main/java/tech/kayys/wayang/node/websearch/enhancer/EnhancementPipeline.java`

```java
package tech.kayys.wayang.node.websearch.enhancer;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.core.execution.NodeContext;
import tech.kayys.wayang.node.websearch.api.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhancement Pipeline - Enriches search results
 * 
 * Available enhancers:
 * - ContentExtractor: Extract full page content
 * - EntityExtractor: Named entity recognition
 * - Summarizer: Generate summaries
 * - Translator: Translate content
 * - ScreenshotCapture: Visual snapshots
 * - QualityScorer: Relevance and quality scores
 */
@ApplicationScoped
public class EnhancementPipeline {

    @Inject
    Instance<ResultEnhancer> enhancers;

    public Uni<SearchResponse> enhance(
        SearchResponse response, 
        SearchRequest request,
        NodeContext context
    ) {
        // Get enabled enhancers based on request configuration
        List<ResultEnhancer> activeEnhancers = getActiveEnhancers(request);

        if (activeEnhancers.isEmpty()) {
            return Uni.createFrom().item(response);
        }

        // Apply enhancers sequentially to each result
        List<Uni<EnhancedResult>> enhancedResults = response.getResults().stream()
            .map(result -> applyEnhancers(result, activeEnhancers, context))
            .collect(Collectors.toList());

        return Uni.combine().all().unis(enhancedResults)
            .combinedWith(results -> {
                response.setResults(
                    results.stream()
                        .map(r -> (SearchResult) r)
                        .collect(Collectors.toList())
                );
                return response;
            });
    }

    private Uni<EnhancedResult> applyEnhancers(
        SearchResult result,
        List<ResultEnhancer> enhancers,
        NodeContext context
    ) {
        Uni<EnhancedResult> current = Uni.createFrom().item(
            EnhancedResult.from(result)
        );

        for (ResultEnhancer enhancer : enhancers) {
            current = current.onItem().transformToUni(
                r -> enhancer.enhance(r, context)
            );
        }

        return current;
    }

    private List<ResultEnhancer> getActiveEnhancers(SearchRequest request) {
        var enhancements = request.getEnhancements();
        if (enhancements == null || enhancements.isEmpty()) {
            return List.of();
        }

        return enhancers.stream()
            .filter(e -> enhancements.contains(e.getEnhancementType()))
            .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
            .collect(Collectors.toList());
    }
}

/**
 * Base interface for result enhancers
 */
public interface ResultEnhancer {
    
    String getEnhancementType();
    
    int getPriority();
    
    Uni<EnhancedResult> enhance(EnhancedResult result, NodeContext context);
}
```

---

## Configuration Examples

### Application Configuration

**File**: `websearch-core/src/main/resources/application.properties`

```properties
# Web Search Node Configuration

# ===== General Settings =====
websearch.enable-multi-provider=false
websearch.parallel-providers=false
websearch.default-max-results=10
websearch.default-timeout=30s

# ===== Cache Settings =====
websearch.cache.enabled=true
websearch.cache.ttl=1h
websearch.cache.max-size=10000
websearch.cache.type=caffeine

# ===== Provider: Google =====
websearch.provider.google.enabled=true
websearch.provider.google.api-key=${GOOGLE_SEARCH_API_KEY}
websearch.provider.google.search-engine-id=${GOOGLE_SEARCH_ENGINE_ID}
websearch.provider.google.rate-limit=100
websearch.provider.google.rate-limit-period=1d
websearch.provider.google.timeout=15s
websearch.provider.google.priority=100

# ===== Provider: Bing =====
websearch.provider.bing.enabled=true
websearch.provider.bing.api-key=${BING_SEARCH_API_KEY}
websearch.provider.bing.rate-limit=1000
websearch.provider.bing.rate-limit-period=1mo
websearch.provider.bing.timeout=15s
websearch.provider.bing.priority=90

# ===== Provider: DuckDuckGo =====
websearch.provider.duckduckgo.enabled=true
websearch.provider.duckduckgo.rate-limit=unlimited
websearch.provider.duckduckgo.timeout=20s
websearch.provider.duckduckgo.priority=70

# ===== Provider: SerpAPI =====
websearch.provider.serpapi.enabled=false
websearch.provider.serpapi.api-key=${SERPAPI_KEY}
websearch.provider.serpapi.rate-limit=100
websearch.provider.serpapi.rate-limit-period=1mo

# ===== Capabilities =====
websearch.capability.text.enabled=true
websearch.capability.image.enabled=true
websearch.capability.video.enabled=true
websearch.capability.news.enabled=true
websearch.capability.academic.enabled=true
websearch.capability.shopping.enabled=false
websearch.capability.maps.enabled=true
websearch.capability.scrape.enabled=true
websearch.capability.semantic.enabled=false

# ===== Web Scraping =====
websearch.scrape.respect-robots-txt=true
websearch.scrape.user-agent=WayangBot/1.0
websearch.scrape.timeout=30s
websearch.scrape.max-content-size=5MB
websearch.scrape.javascript-rendering=false

# ===== Enhancements =====
websearch.enhancement.extract.enabled=true
websearch.enhancement.entity.enabled=true
websearch.enhancement.entity.model=en_core_web_sm
websearch.enhancement.summarize.enabled=true
websearch.enhancement.summarize.max-length=500
websearch.enhancement.translate.enabled=false
websearch.enhancement.screenshot.enabled=false
websearch.enhancement.quality.enabled=true

# ===== Rate Limiting =====
websearch.ratelimit.enabled=true
websearch.ratelimit.per-tenant=true
websearch.ratelimit.default-limit=100
websearch.ratelimit.default-period=1h

# ===== Circuit Breaker =====
websearch.circuit-breaker.enabled=true
websearch.circuit-breaker.failure-threshold=5
websearch.circuit-breaker.delay=60s
websearch.circuit-breaker.success-threshold=2

# ===== Retry Policy =====
websearch.retry.enabled=true
websearch.retry.max-attempts=3
websearch.retry.backoff=exponential
websearch.retry.initial-delay=1s
websearch.retry.max-delay=10s

# ===== Observability =====
websearch.telemetry.enabled=true
websearch.telemetry.trace-all-requests=true
websearch.telemetry.metrics.enabled=true

# ===== Audit =====
websearch.audit.enabled=true
websearch.audit.log-queries=true
websearch.audit.log-results=false
websearch.audit.retention-days=90
```

---

## Node Schema Definition

**File**: `websearch-core/src/main/resources/node-schema/web-search-node.yaml`

```yaml
id: "wayang/web-search"
type: "integration.web-search"
version: "1.0.0"

author:
  name: "Wayang Team"
  email: "team@kayys.tech"
  organization: "kayys.tech"

description: |
  Multi-provider web search node with advanced capabilities including
  text search, image search, video search, news search, web scraping,
  and content enhancement.

properties:
  - name: "searchType"
    type: "string"
    required: true
    default: "text"
    enum:
      - "text"
      - "image"
      - "video"
      - "news"
      - "academic"
      - "shopping"
      - "maps"
      - "code"
    description: "Type of search to perform"

  - name: "providers"
    type: "array"
    required: false
    default: ["google", "bing", "duckduckgo"]
    items:
      type: "string"
    description: "Preferred search providers (in priority order)"

  - name: "maxResults"
    type: "integer"
    required: false
    default: 10
    minimum: 1
    maximum: 100
    description: "Maximum number of results to return"

  - name: "enableCache"
    type: "boolean"
    required: false
    default: true
    description: "Enable result caching"

  - name: "enableEnhancements"
    type: "boolean"
    required: false
    default: false
    description: "Enable content enhancements (extraction, NER, etc.)"

  - name: "enhancements"
    type: "array"
    required: false
    items:
      type: "string"
      enum:
        - "extract"
        - "entity"
        - "summarize"
        - "translate"
        - "screenshot"
        - "quality"
    description: "Specific enhancements to apply"

  - name: "locale"
    type: "string"
    required: false
    default: "en"
    pattern: "^[a-z]{2}(-[A-Z]{2})?$"
    description: "Search locale (e.g., 'en', 'en-US', 'fr')"

  - name: "safeSearch"
    type: "boolean"
    required: false
    default: true
    description: "Enable safe search filtering"

  - name: "filters"
    type: "object"
    required: false
    description: "Additional search filters (time range, domain, etc.)"
    properties:
      timeRange:
        type: "string"
        enum: ["hour", "day", "week", "month", "year", "all"]
      domains:
        type: "array"
        items:
          type: "string"
      excludeDomains:
        type: "array"
        items:
          type: "string"
      language:
        type: "string"
      country:
        type: "string"

inputs:
  - name: "query"
    type: "string"
    required: true
    description: "Search query"
    validation:
      minLength: 1
      maxLength: 1000

  - name: "context"
    type: "string"
    required: false
    description: "Additional context to improve search relevance"

outputs:
  success:
    - name: "results"
      type: "array"
      description: "Array of search results"
      schema:
        items:
          type: "object"
          properties:
            title:
              type: "string"
            url:
              type: "string"
              format: "uri"
            snippet:
              type: "string"
            displayUrl:
              type: "string"
            publishedDate:
              type: "string"
              format: "date-time"
            source:
              type: "string"
            thumbnail:
              type: "string"
              format: "uri"
            score:
              type: "number"
            metadata:
              type: "object"

    - name: "metadata"
      type: "object"
      description: "Search metadata"
      properties:
        totalResults:
          type: "integer"
        searchDuration:
          type: "integer"
        providerUsed:
          type: "string"
        cacheHit:
          type: "boolean"
        enhancementsApplied:
          type: "array"

  error:
    type: "ErrorPayload"
    schema:
      $ref: "https://kayys.tech/schema/v1/concerns/error.schema.json"

errorHandling:
  retryPolicy:
    enabled: true
    maxAttempts: 3
    backoffStrategy: "exponential"
    initialDelay: "1s"
    maxDelay: "10s"
  
  fallbackProviders: true
  
  timeoutMs: 30000
  
  circuitBreaker:
    enabled: true
    failureThreshold: 5
    resetTimeout: "60s"

observability:
  tracing:
    enabled: true
    attributes:
      - "query"
      - "searchType"
      - "provider"
      - "resultCount"
  
  metrics:
    enabled: true
    counters:
      - "searches_total"
      - "search_errors_total"
      - "cache_hits_total"
    histograms:
      - "search_duration_seconds"
      - "result_count"
    gauges:
      - "active_searches"

audit:
  enabled: true
  logQueries: true
  logResults: false
  sensitiveFields:
    - "query"  # PII detection

resourceProfile:
  cpu: "0.5"
  memory: "512Mi"
  timeout: "30s"
  concurrent: 10

capabilities:
  - "network"
  - "tool_execution"

sandboxLevel: "semi-trusted"

tags:
  - "search"
  - "web"
  - "integration"
  - "data-source"
```

---

## Deployment & Packaging

### Maven POM Structure

**File**: `websearch-core/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>tech.kayys.wayang</groupId>
        <artifactId>wayang-nodes-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>wayang-node-websearch-core</artifactId>
    <name>Wayang :: Nodes :: WebSearch :: Core</name>

    <dependencies>
        <!-- Wayang Core -->
        <dependency>
            <groupId>tech.kayys.wayang</groupId>
            <artifactId>wayang-core-node-api</artifactId>
        </dependency>
        <dependency>
            <groupId>tech.kayys.wayang</groupId>
            <artifactId>wayang-core-execution-api</artifactId>
        </dependency>

        <!-- Quarkus -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-arc</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-client-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-client-reactive-jackson</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-opentelemetry</artifactId>
        </dependency>

        <!-- Reactive -->
        <dependency>
            <groupId>io.smallrye.reactive</groupId>
            <artifactId>mutiny</artifactId>
        </dependency>

        <!-- Resilience -->
        <dependency>
            <groupId>io.smallrye</groupId>
            <artifactId>smallrye-fault-tolerance</artifactId>
        </dependency>

        <!-- Web Scraping -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.17.2</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Error Handling & Audit Integration

### Error Handling Example

```java
@Override
protected Uni<ExecutionResult> executeIntegration(NodeContext context) {
    SearchRequest request = parseSearchRequest(context);
    
    return orchestrator.search(request, context)
        .onItem().transform(this::toExecutionResult)
        .onFailure(RateLimitException.class).recoverWithItem(error -> {
            // Error as new input - route to error port
            ErrorPayload errorPayload = ErrorPayload.builder()
                .type("RateLimitError")
                .message("Search quota exceeded for provider: " + error.getProvider())
                .details(Map.of(
                    "provider", error.getProvider(),
                    "quotaLimit", error.getLimit(),
                    "resetTime", error.getResetTime()
                ))
                .retryable(true)
                .originNode(descriptor.getId())
                .timestamp(Instant.now())
                .build();
            
            return ExecutionResult.error(errorPayload);
        })
        .onFailure(ProviderException.class).recoverWithUni(error -> {
            // Try fallback provider
            if (request.getProviders().size() > 1) {
                SearchRequest fallbackRequest = request.withNextProvider();
                return orchestrator.search(fallbackRequest, context)
                    .onItem().transform(this::toExecutionResult);
            }
            return Uni.createFrom().item(
                ExecutionResult.error(createProviderError(error))
            );
        })
        .onFailure().recoverWithItem(error -> {
            // Generic error handling
            return ExecutionResult.error(
                ErrorPayload.from(error, descriptor.getId(), context)
            );
        });
}
```

### Audit Integration

```java
@Inject
AuditService auditService;

private void logSearchAudit(SearchRequest request, SearchResponse response, NodeContext context) {
    AuditEntry entry = AuditEntry.builder()
        .event("WEB_SEARCH_EXECUTED")
        .level(AuditLevel.INFO)
        .actor(ActorInfo.system())
        .resource(ResourceInfo.builder()
            .type("web-search")
            .id(context.getNodeId())
            .build())
        .metadata(Map.of(
            "query", request.getQuery(),
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
```

---

## Next Steps & Recommendations

### 1. **Provider Implementations**
Create additional providers:
- BingSearchProvider
- DuckDuckGoProvider
- BraveSearchProvider
- SerpApiProvider
- Custom/Self-hosted provider

### 2. **Advanced Capabilities**
Implement specialized search types:
- Academic search (Google Scholar, Semantic Scholar)
- Code search (GitHub, GitLab)
- Shopping/product search
- Semantic/vector search integration

### 3. **Enhancement Modules**
Build enhancement pipeline:
- NER with spaCy/Stanford NLP
- Summarization with LLM
- Translation service
- Screenshot capture with Playwright
- Quality scoring algorithms

### 4. **Optimization**
- Result caching strategies
- Query optimization
- Provider cost optimization
- Parallel search aggregation

### 5. **Testing**
- Unit tests for each provider
- Integration tests with mock providers
- Load testing
- Cost simulation

Would you like me to create:
1. Complete implementation files for any specific component?
2. Additional provider implementations?
3. Testing suite?
4. Deployment configurations?
5. Standalone runtime packaging?
