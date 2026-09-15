package tech.kayys.wayang.security.obligation;

import java.util.Map;

/**
 * Result returned by an {@link ObligationExecutor}.
 */
public record ObligationResult(
        boolean successful,
        boolean continueExecution,
        String reason,
        Map<String, Object> attributes
) {

    public ObligationResult {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static ObligationResult success() {
        return new ObligationResult(true, true, null, Map.of());
    }

    public static ObligationResult success(Map<String, Object> attributes) {
        return new ObligationResult(true, true, null, attributes);
    }

    public static ObligationResult stop(String reason) {
        return new ObligationResult(true, false, reason, Map.of());
    }

    public static ObligationResult failure(String reason) {
        return new ObligationResult(false, false, reason, Map.of());
    }
}
