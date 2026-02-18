package tech.kayys.wayang.eip.config;

import java.time.Duration;
import java.util.Map;

public record ChannelConfig(String channelName, String type, int capacity, boolean persistent, Duration ttl) {
    public static ChannelConfig fromContext(Map<String, Object> context) {
        return new ChannelConfig(
                (String) context.get("channelName"),
                (String) context.getOrDefault("type", "queue"),
                (Integer) context.getOrDefault("capacity", 1000),
                (Boolean) context.getOrDefault("persistent", true),
                Duration.ofSeconds((Integer) context.getOrDefault("ttlSeconds", 3600)));
    }
}
