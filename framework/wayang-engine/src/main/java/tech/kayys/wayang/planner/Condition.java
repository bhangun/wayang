package tech.kayys.wayang.planner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;

/**
 * Condition for plan steps
 */
public record Condition(
    String type,
    String expression,
    Map<String, Object> values
) {
    public static Condition always() {
        return new Condition("always", "true", Map.of());
    }
    
    public static Condition never() {
        return new Condition("never", "false", Map.of());
    }
    
    public static Condition expression(String expression) {
        return new Condition("expression", expression, Map.of());
    }
}