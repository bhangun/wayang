package tech.kayys.wayang.node.websearch;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import tech.kayys.wayang.node.websearch.exception.ProviderException;
import tech.kayys.wayang.node.websearch.api.*;
import tech.kayys.wayang.node.websearch.spi.*;

import java.time.Duration;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@ApplicationScoped
public class SearchOrchestrator {
    
    private static final Logger LOG = Logger.getLogger(SearchOrchestrator.class);
    
    @Inject
    Instance<SearchProvider> providers;

    @Inject
    ProviderConfigFactory providerConfigFactory;

    @Inject
    ProviderCircuitBreakerRegistry circuitBreakerRegistry;

    public Uni<SearchResponse> search(SearchRequest request) {
        LOG.infof("Search: query='%s', type=%s, max=%d", 
            request.query(), request.searchType(), request.maxResults());
        
        List<SearchProvider> available = selectProviders(request);
        
        if (available.isEmpty()) {
            LOG.warnf("No search providers available for type=%s providers=%s",
                request.searchType(), request.providers());
            return Uni.createFrom().item(fallbackResponse(request.query()));
        }

        return executeWithFallback(request, available, 0);
    }

    private Uni<SearchResponse> executeWithFallback(SearchRequest request, List<SearchProvider> provs, int idx) {
        if (idx >= provs.size()) {
            LOG.warnf("All providers failed for query='%s', returning empty results", request.query());
            return Uni.createFrom().item(fallbackResponse(request.query()));
        }

        SearchProvider provider = provs.get(idx);
        ProviderConfig providerConfig = providerConfigFactory.forProvider(request, provider.getProviderId());

        if (!providerConfig.getBoolean("enabled", true)) {
            LOG.debugf("Provider %s disabled by config", provider.getProviderId());
            return executeWithFallback(request, provs, idx + 1);
        }

        if (!circuitBreakerRegistry.allowRequest(provider.getProviderId(), providerConfig)) {
            LOG.warnf("Provider %s skipped because circuit breaker is open", provider.getProviderId());
            return executeWithFallback(request, provs, idx + 1);
        }

        long startNanos = System.nanoTime();
        long timeoutMs = Math.max(200L, providerConfig.getLong("timeout-ms", 2500L));
        int maxRetries = Math.max(0, providerConfig.getInt("max-retries", 1));
        long retryBackoffMs = Math.max(50L, providerConfig.getLong("retry-backoff-ms", 150L));
        long rateLimitBackoffMs = Math.max(100L, providerConfig.getLong("rate-limit-retry-backoff-ms", 1000L));

        Uni<ProviderSearchResult> providerExecution = Uni.createFrom().deferred(() ->
            provider.search(request, providerConfig)
                .ifNoItem().after(Duration.ofMillis(timeoutMs)).failWith(
                    () -> new TimeoutException("Provider " + provider.getProviderId() + " timed out after " + timeoutMs + "ms")
                )
        );

        if (maxRetries > 0) {
            providerExecution = providerExecution
                .onFailure(this::isRateLimitedFailure)
                .retry()
                .withBackOff(Duration.ofMillis(rateLimitBackoffMs), Duration.ofMillis(rateLimitBackoffMs * 4))
                .atMost(maxRetries)
                .onFailure(this::isNonRateRetriableFailure)
                .retry()
                .withBackOff(Duration.ofMillis(retryBackoffMs), Duration.ofMillis(retryBackoffMs * 4))
                .atMost(maxRetries);
        }

        return providerExecution
            .onItem().transform(result -> {
                long duration = (System.nanoTime() - startNanos) / 1_000_000;
                circuitBreakerRegistry.recordSuccess(provider.getProviderId());
                LOG.infof("Provider %s: %d results in %dms", 
                    provider.getProviderId(), result.results().size(), duration);
                return SearchResponse.builder()
                    .results(result.results())
                    .totalResults(result.totalResults())
                    .providerUsed(provider.getProviderId())
                    .durationMs(duration)
                    .build();
            })
            .onFailure().recoverWithUni(err -> {
                circuitBreakerRegistry.recordFailure(provider.getProviderId(), providerConfig);
                LOG.warnf("Provider %s failed: %s", provider.getProviderId(), err.getMessage());
                return executeWithFallback(request, provs, idx + 1);
            });
    }

    private List<SearchProvider> selectProviders(SearchRequest request) {
        SearchCapability capability = request.getCapability();
        Set<String> requestedProviders = request.providers().stream()
            .map(id -> id.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        return providers.stream()
            .filter(SearchProvider::isEnabled)
            .filter(p -> requestedProviders.contains(p.getProviderId().toLowerCase(Locale.ROOT)))
            .filter(p -> p.getSupportedCapabilities().contains(capability))
            .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
            .collect(Collectors.toList());
    }

    private boolean isRateLimitedFailure(Throwable throwable) {
        ProviderException providerException = asProviderException(throwable);
        return providerException != null && providerException.isRateLimited();
    }

    private boolean isNonRateRetriableFailure(Throwable throwable) {
        ProviderException providerException = asProviderException(throwable);
        if (providerException != null) {
            return providerException.isRetryable() && !providerException.isRateLimited();
        }
        return !(throwable instanceof IllegalArgumentException);
    }

    private ProviderException asProviderException(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof ProviderException providerException) {
                return providerException;
            }
            cursor = cursor.getCause();
        }
        return null;
    }

    private SearchResponse fallbackResponse(String query) {
        if (query == null || query.isBlank()) {
            return SearchResponse.builder()
                .results(List.of())
                .totalResults(0)
                .providerUsed("none")
                .durationMs(0L)
                .build();
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        SearchResult fallback = SearchResult.builder()
            .title("Search web for '" + query + "'")
            .url("https://duckduckgo.com/?q=" + encodedQuery)
            .snippet("Live providers are unavailable. Open this URL to view web results directly.")
            .source("fallback")
            .score(50.0)
            .build();

        return SearchResponse.builder()
            .results(List.of(fallback))
            .totalResults(1)
            .providerUsed("fallback")
            .durationMs(0L)
            .build();
    }
}
