package tech.kayys.wayang.node.websearch;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.kayys.wayang.node.websearch.api.SearchRequest;
import tech.kayys.wayang.node.websearch.spi.ProviderConfig;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ProviderConfigFactory {

    private static final String BASE_KEY = "wayang.websearch";

    private static final String KEY_ENDPOINT = "endpoint";
    private static final String KEY_API_KEY = "api-key";
    private static final String KEY_TIMEOUT_MS = "timeout-ms";
    private static final String KEY_MAX_RETRIES = "max-retries";
    private static final String KEY_RETRY_BACKOFF_MS = "retry-backoff-ms";
    private static final String KEY_RATE_LIMIT_RETRY_BACKOFF_MS = "rate-limit-retry-backoff-ms";
    private static final String KEY_CIRCUIT_BREAKER_FAILURE_THRESHOLD = "circuit-breaker-failure-threshold";
    private static final String KEY_CIRCUIT_BREAKER_OPEN_MS = "circuit-breaker-open-ms";
    private static final String KEY_ENABLED = "enabled";

    public ProviderConfig forProvider(SearchRequest request, String providerId) {
        String tenantId = request.tenantId();
        Config config = ConfigProvider.getConfig();
        Map<String, String> properties = new HashMap<>();

        // Lower precedence first
        mergeScopedProperties(config, resiliencePrefix(), properties);
        mergeScopedProperties(config, providerPrefix(providerId), properties);
        mergeScopedProperties(config, tenantProviderPrefix(tenantId, providerId), properties);

        // Ensure baseline defaults when no config is provided.
        properties.putIfAbsent(KEY_TIMEOUT_MS, "2500");
        properties.putIfAbsent(KEY_MAX_RETRIES, "1");
        properties.putIfAbsent(KEY_RETRY_BACKOFF_MS, "150");
        properties.putIfAbsent(KEY_RATE_LIMIT_RETRY_BACKOFF_MS, "1000");
        properties.putIfAbsent(KEY_CIRCUIT_BREAKER_FAILURE_THRESHOLD, "5");
        properties.putIfAbsent(KEY_CIRCUIT_BREAKER_OPEN_MS, "30000");
        properties.putIfAbsent(KEY_ENABLED, "true");

        return ProviderConfig.forProvider(providerId, properties);
    }

    private void mergeScopedProperties(Config config, String prefix, Map<String, String> target) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        for (String name : config.getPropertyNames()) {
            if (!name.startsWith(prefix)) {
                continue;
            }
            String suffix = name.substring(prefix.length());
            if (suffix.isBlank()) {
                continue;
            }
            config.getOptionalValue(name, String.class)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .ifPresent(value -> target.put(suffix, value));
        }
    }

    private String providerPrefix(String providerId) {
        return BASE_KEY + ".providers." + providerId + ".";
    }

    private String tenantProviderPrefix(String tenantId, String providerId) {
        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }
        return BASE_KEY + ".tenants." + tenantId + ".providers." + providerId + ".";
    }

    private String resiliencePrefix() {
        return BASE_KEY + ".resilience.";
    }
}
