package tech.kayys.wayang.integration.core.config;

import java.util.Map;

public record RouteRule(String condition, String targetNode, int priority) {
    public static RouteRule fromMap(Map<String, Object> map) {
        return new RouteRule(
                (String) map.get("condition"),
                (String) map.get("targetNode"),
                (Integer) map.getOrDefault("priority", 0));
    }
}
