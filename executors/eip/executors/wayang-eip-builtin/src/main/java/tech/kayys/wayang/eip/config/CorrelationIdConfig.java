package tech.kayys.wayang.eip.config;

import java.util.Map;

public record CorrelationIdConfig(
        String strategy,
        String extractFrom,
        String headerName) {
    public static CorrelationIdConfig fromContext(Map<String, Object> context) {
        return new CorrelationIdConfig(
                (String) context.getOrDefault("strategy", "generate"),
                (String) context.get("extractFrom"),
                (String) context.getOrDefault("headerName", "X-Correlation-ID"));
    }
}
