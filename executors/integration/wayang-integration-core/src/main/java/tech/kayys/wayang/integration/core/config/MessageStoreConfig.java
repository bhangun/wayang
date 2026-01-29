package tech.kayys.wayang.integration.core.config;

import java.time.Duration;
import java.util.Map;

public record MessageStoreConfig(String storeType, Duration retention, boolean compressed) {
    public static MessageStoreConfig fromContext(Map<String, Object> context) {
        return new MessageStoreConfig(
                (String) context.getOrDefault("storeType", "in-memory"),
                Duration.ofDays((Integer) context.getOrDefault("retentionDays", 30)),
                (Boolean) context.getOrDefault("compressed", false));
    }
}
