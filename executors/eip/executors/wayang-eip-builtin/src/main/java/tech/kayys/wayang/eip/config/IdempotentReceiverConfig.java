package tech.kayys.wayang.eip.config;

import java.time.Duration;
import java.util.Map;

public record IdempotentReceiverConfig(String idempotencyKeyField, Duration windowDuration, String action) {
    public static IdempotentReceiverConfig fromContext(Map<String, Object> context) {
        return new IdempotentReceiverConfig(
                (String) context.getOrDefault("idempotencyKeyField", "messageId"),
                Duration.ofHours((Integer) context.getOrDefault("windowHours", 24)),
                (String) context.getOrDefault("action", "skip"));
    }
}
