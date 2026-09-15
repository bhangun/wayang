package tech.kayys.wayang.security.enforcement;

import java.util.Map;

/**
 * Outcome of enforcement by a {@link PolicyEnforcementPoint}.
 * If allowed, {@code data} contains the (potentially redacted or masked) execution payload.
 */
public record EnforcementResult(
        boolean allowed,
        Object data,
        String reason,
        Map<String, Object> attributes
) {

    public EnforcementResult {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static EnforcementResult allow(Object data) {
        return new EnforcementResult(true, data, null, Map.of());
    }

    public static EnforcementResult allow(Object data, Map<String, Object> attributes) {
        return new EnforcementResult(true, data, null, attributes);
    }

    public static EnforcementResult deny(String reason) {
        return new EnforcementResult(false, null, reason, Map.of());
    }
}
