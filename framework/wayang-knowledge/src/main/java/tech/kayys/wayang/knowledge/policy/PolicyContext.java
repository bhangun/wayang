package tech.kayys.wayang.knowledge.policy;

import java.util.Map;

/**
 * Execution context provided when evaluating policies.
 */
public record PolicyContext(
        String tenantId,
        String userId,
        String domain,
        String scope,
        String operation,
        Map<String, Object> attributes
) {

    public PolicyContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        scope = scope == null ? "default" : scope;
        operation = operation == null ? "evaluate" : operation;
    }

    public static PolicyContext of(String tenantId, String userId, String domain) {
        return new PolicyContext(tenantId, userId, domain, "default", "evaluate", Map.of());
    }
}
