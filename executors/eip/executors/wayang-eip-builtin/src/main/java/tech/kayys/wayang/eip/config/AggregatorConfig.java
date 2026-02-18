package tech.kayys.wayang.eip.config;

import java.time.Duration;
import java.util.Map;

public record AggregatorConfig(String correlationKey, int expectedCount, Duration timeout,
        String strategy, boolean completeOnTimeout) {
    public static AggregatorConfig fromContext(Map<String, Object> context) {
        return new AggregatorConfig(
                (String) context.get("correlationKey"),
                (Integer) context.getOrDefault("expectedCount", -1),
                Duration.ofSeconds((Integer) context.getOrDefault("timeoutSeconds", 60)),
                (String) context.getOrDefault("strategy", "collect-all"),
                (Boolean) context.getOrDefault("completeOnTimeout", true));
    }
}
