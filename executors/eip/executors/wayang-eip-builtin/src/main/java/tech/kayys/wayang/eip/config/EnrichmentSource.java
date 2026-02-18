package tech.kayys.wayang.eip.config;

import java.util.Map;

public record EnrichmentSource(String type, String uri, Map<String, String> mapping) {
    @SuppressWarnings("unchecked")
    public static EnrichmentSource fromMap(Map<String, Object> map) {
        return new EnrichmentSource(
                (String) map.get("type"),
                (String) map.get("uri"),
                (Map<String, String>) map.getOrDefault("mapping", Map.of()));
    }
}
