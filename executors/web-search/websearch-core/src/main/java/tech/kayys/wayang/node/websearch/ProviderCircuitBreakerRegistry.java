package tech.kayys.wayang.node.websearch;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.node.websearch.spi.ProviderConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class ProviderCircuitBreakerRegistry {

    private static final String KEY_FAILURE_THRESHOLD = "circuit-breaker-failure-threshold";
    private static final String KEY_OPEN_MS = "circuit-breaker-open-ms";

    private final ConcurrentMap<String, ProviderCircuitState> states = new ConcurrentHashMap<>();

    public boolean allowRequest(String providerId, ProviderConfig config) {
        ProviderCircuitState state = states.computeIfAbsent(providerId, ignored -> new ProviderCircuitState());
        return state.allowRequest();
    }

    public void recordSuccess(String providerId) {
        ProviderCircuitState state = states.computeIfAbsent(providerId, ignored -> new ProviderCircuitState());
        state.recordSuccess();
    }

    public void recordFailure(String providerId, ProviderConfig config) {
        ProviderCircuitState state = states.computeIfAbsent(providerId, ignored -> new ProviderCircuitState());
        int threshold = Math.max(1, config.getInt(KEY_FAILURE_THRESHOLD, 5));
        long openMs = Math.max(1000L, config.getLong(KEY_OPEN_MS, 30000L));
        state.recordFailure(threshold, openMs);
    }

    private static final class ProviderCircuitState {
        private int consecutiveFailures;
        private long openUntilEpochMs;

        private synchronized boolean allowRequest() {
            return System.currentTimeMillis() >= openUntilEpochMs;
        }

        private synchronized void recordSuccess() {
            consecutiveFailures = 0;
            openUntilEpochMs = 0;
        }

        private synchronized void recordFailure(int threshold, long openMs) {
            consecutiveFailures++;
            if (consecutiveFailures >= threshold) {
                openUntilEpochMs = System.currentTimeMillis() + openMs;
                consecutiveFailures = 0;
            }
        }
    }
}
