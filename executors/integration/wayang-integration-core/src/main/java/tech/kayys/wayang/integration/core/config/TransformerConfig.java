package tech.kayys.wayang.integration.core.config;

import java.util.Map;

public record TransformerConfig(String transformType, String script, Map<String, Object> parameters) {
    @SuppressWarnings("unchecked")
    public static TransformerConfig fromContext(Map<String, Object> context) {
        return new TransformerConfig(
                (String) context.getOrDefault("transformType", "custom"),
                (String) context.get("script"),
                (Map<String, Object>) context.getOrDefault("parameters", Map.of()));
    }
}
