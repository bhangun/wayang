package tech.kayys.wayang.integration.core.config;

import java.util.Map;

public record FilterConfig(String expression, boolean inverse, String onFilteredRoute) {
    public static FilterConfig fromContext(Map<String, Object> context) {
        return new FilterConfig(
                (String) context.get("expression"),
                (Boolean) context.getOrDefault("inverse", false),
                (String) context.get("onFilteredRoute"));
    }
}
