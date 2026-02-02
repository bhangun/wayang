package tech.kayys.wayang.integration.core.config;

import java.time.Duration;
import java.util.Map;

public record DeadLetterChannelConfig(
        String channelName,
        boolean logErrors,
        boolean notifyAdmin,
        Duration retention) {
    public static DeadLetterChannelConfig fromContext(Map<String, Object> context) {
        return new DeadLetterChannelConfig(
                (String) context.getOrDefault("channelName", "dead-letter"),
                (Boolean) context.getOrDefault("logErrors", true),
                (Boolean) context.getOrDefault("notifyAdmin", false),
                Duration.ofDays((Integer) context.getOrDefault("retentionDays", 7)));
    }
}
