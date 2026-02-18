package tech.kayys.wayang.eip.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public record RetryConfig(int maxAttempts, Duration initialDelay, Duration maxDelay,
        double backoffMultiplier, List<Class<? extends Throwable>> retryableExceptions) {
    public static RetryConfig fromContext(Map<String, Object> context) {
        return new RetryConfig(
                (Integer) context.getOrDefault("maxAttempts", 3),
                Duration.ofMillis((Integer) context.getOrDefault("initialDelayMs", 1000)),
                Duration.ofMillis((Integer) context.getOrDefault("maxDelayMs", 60000)),
                (Double) context.getOrDefault("backoffMultiplier", 2.0),
                List.of(Exception.class));
    }
}
