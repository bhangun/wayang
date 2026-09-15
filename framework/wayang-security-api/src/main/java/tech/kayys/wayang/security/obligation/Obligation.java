package tech.kayys.wayang.security.obligation;

import java.util.Map;
import java.util.Objects;

/**
 * An obligation attached to an authorization decision that must be enforced during execution.
 */
public record Obligation(
        ObligationType type,
        ObligationPhase phase,
        Map<String, Object> parameters
) {

    public Obligation {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(phase, "phase");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public static Obligation of(ObligationType type, ObligationPhase phase) {
        return new Obligation(type, phase, Map.of());
    }

    public static Obligation of(ObligationType type, ObligationPhase phase, Map<String, Object> parameters) {
        return new Obligation(type, phase, parameters);
    }

    public static Obligation before(ObligationType type) {
        return of(type, ObligationPhase.BEFORE_EXECUTION);
    }

    public static Obligation during(ObligationType type) {
        return of(type, ObligationPhase.DURING_EXECUTION);
    }

    public static Obligation after(ObligationType type) {
        return of(type, ObligationPhase.AFTER_EXECUTION);
    }
}
