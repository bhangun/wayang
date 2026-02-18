package tech.kayys.wayang.node.websearch;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.node.websearch.api.ProviderSearchResult;
import tech.kayys.wayang.node.websearch.api.SearchCapability;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.api.SearchResult;
import tech.kayys.wayang.node.websearch.exception.ProviderException;
import tech.kayys.wayang.node.websearch.spi.ProviderConfig;
import tech.kayys.wayang.node.websearch.spi.SearchProvider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchOrchestratorResilienceTest {

    private static final String RETRY_PROP = "wayang.websearch.resilience.max-retries";
    private static final String RATE_LIMIT_BACKOFF_PROP = "wayang.websearch.resilience.rate-limit-retry-backoff-ms";
    private static final String RETRY_BACKOFF_PROP = "wayang.websearch.resilience.retry-backoff-ms";

    @AfterEach
    void cleanupProps() {
        System.clearProperty(RETRY_PROP);
        System.clearProperty(RATE_LIMIT_BACKOFF_PROP);
        System.clearProperty(RETRY_BACKOFF_PROP);
    }

    @Test
    void retriesRateLimitedProviderThenSucceeds() {
        System.setProperty(RETRY_PROP, "1");
        System.setProperty(RATE_LIMIT_BACKOFF_PROP, "1");
        System.setProperty(RETRY_BACKOFF_PROP, "1");

        AtomicInteger attempts = new AtomicInteger();
        SearchProvider provider = new CountingProvider("rate-provider", 100, request -> {
            if (attempts.incrementAndGet() == 1) {
                return Uni.createFrom().failure(new ProviderException("rate-provider", 429, "Too Many Requests"));
            }
            return Uni.createFrom().item(successResult("rate-provider"));
        });

        SearchOrchestrator orchestrator = newOrchestrator(List.of(provider));
        SearchRequest request = SearchRequest.builder()
            .query("resilience")
            .providers(List.of("rate-provider"))
            .build();

        var response = orchestrator.search(request).await().indefinitely();

        assertEquals("rate-provider", response.providerUsed());
        assertEquals(2, attempts.get());
    }

    @Test
    void doesNotRetryNonRetriableClientErrorAndFallsBack() {
        System.setProperty(RETRY_PROP, "3");
        System.setProperty(RATE_LIMIT_BACKOFF_PROP, "1");
        System.setProperty(RETRY_BACKOFF_PROP, "1");

        AtomicInteger firstAttempts = new AtomicInteger();
        SearchProvider first = new CountingProvider("first-provider", 100, request -> {
            firstAttempts.incrementAndGet();
            return Uni.createFrom().failure(new ProviderException("first-provider", 400, "Bad Request"));
        });

        AtomicInteger secondAttempts = new AtomicInteger();
        SearchProvider second = new CountingProvider("second-provider", 90, request -> {
            secondAttempts.incrementAndGet();
            return Uni.createFrom().item(successResult("second-provider"));
        });

        SearchOrchestrator orchestrator = newOrchestrator(List.of(first, second));
        SearchRequest request = SearchRequest.builder()
            .query("fallback")
            .providers(List.of("first-provider", "second-provider"))
            .build();

        var response = orchestrator.search(request).await().indefinitely();

        assertEquals("second-provider", response.providerUsed());
        assertEquals(1, firstAttempts.get());
        assertEquals(1, secondAttempts.get());
    }

    private SearchOrchestrator newOrchestrator(List<SearchProvider> providerList) {
        @SuppressWarnings("unchecked")
        Instance<SearchProvider> providers = mock(Instance.class);
        when(providers.stream()).thenReturn(providerList.stream());

        SearchOrchestrator orchestrator = new SearchOrchestrator();
        orchestrator.providers = providers;
        orchestrator.providerConfigFactory = new ProviderConfigFactory();
        orchestrator.circuitBreakerRegistry = new ProviderCircuitBreakerRegistry();
        return orchestrator;
    }

    private ProviderSearchResult successResult(String providerId) {
        return ProviderSearchResult.builder()
            .providerId(providerId)
            .results(List.of(SearchResult.builder()
                .title("ok")
                .url("https://example.com/" + providerId)
                .snippet("ok")
                .source(providerId)
                .score(100.0)
                .build()))
            .totalResults(1)
            .durationMs(1)
            .build();
    }

    @FunctionalInterface
    interface SearchBehavior {
        Uni<ProviderSearchResult> apply(SearchRequest request);
    }

    static final class CountingProvider implements SearchProvider {
        private final String providerId;
        private final int priority;
        private final SearchBehavior behavior;

        CountingProvider(String providerId, int priority, SearchBehavior behavior) {
            this.providerId = providerId;
            this.priority = priority;
            this.behavior = behavior;
        }

        @Override
        public String getProviderId() {
            return providerId;
        }

        @Override
        public String getProviderName() {
            return providerId;
        }

        @Override
        public Set<SearchCapability> getSupportedCapabilities() {
            return Set.of(SearchCapability.TEXT_SEARCH);
        }

        @Override
        public Uni<ProviderSearchResult> search(SearchRequest request, ProviderConfig config) {
            return behavior.apply(request);
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}
