package tech.kayys.wayang.eip.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record RouterConfig(List<RouteRule> rules, String defaultRoute, boolean failOnNoMatch, String strategy) {

    public Map<String, String> routes() {
        return rules.stream().collect(Collectors.toMap(
                RouteRule::targetNode,
                RouteRule::condition,
                (v1, v2) -> v1 // Keep first on duplicate targets
        ));
    }

    @SuppressWarnings("unchecked")
    public static RouterConfig fromContext(Map<String, Object> context) {
        List<Map<String, Object>> rulesData = (List<Map<String, Object>>) context.getOrDefault("rules", List.of());

        List<RouteRule> rules = rulesData.stream()
                .map(RouteRule::fromMap)
                .toList();

        return new RouterConfig(
                rules,
                (String) context.get("defaultRoute"),
                (Boolean) context.getOrDefault("failOnNoMatch", false),
                (String) context.getOrDefault("strategy", "first"));
    }
}
