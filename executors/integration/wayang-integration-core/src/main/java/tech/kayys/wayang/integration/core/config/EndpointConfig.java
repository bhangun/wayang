package tech.kayys.wayang.integration.core.config;

import java.util.Map;

public record EndpointConfig(String uri, String protocol, Map<String, String> headers,
        Map<String, Object> properties, AuthConfig auth, int timeoutMs, boolean async) {
    @SuppressWarnings("unchecked")
    public static EndpointConfig fromContext(Map<String, Object> context) {
        return new EndpointConfig(
                (String) context.get("uri"),
                (String) context.getOrDefault("protocol", "http"),
                (Map<String, String>) context.getOrDefault("headers", Map.of()),
                (Map<String, Object>) context.getOrDefault("properties", Map.of()),
                AuthConfig.fromContext(context),
                (Integer) context.getOrDefault("timeoutMs", 30000),
                (Boolean) context.getOrDefault("async", false));
    }
}
