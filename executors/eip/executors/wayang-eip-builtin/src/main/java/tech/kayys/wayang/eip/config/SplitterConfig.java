package tech.kayys.wayang.eip.config;

import java.util.Map;

public record SplitterConfig(String strategy, int batchSize, String delimiter, boolean preserveOrder) {
    public static SplitterConfig fromContext(Map<String, Object> context) {
        return new SplitterConfig(
                (String) context.getOrDefault("strategy", "fixed"),
                (Integer) context.getOrDefault("batchSize", 10),
                (String) context.get("delimiter"),
                (Boolean) context.getOrDefault("preserveOrder", true));
    }
}
