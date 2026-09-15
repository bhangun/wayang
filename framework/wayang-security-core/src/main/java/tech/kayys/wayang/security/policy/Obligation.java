package tech.kayys.wayang.security.policy;

import java.util.Map;

/**
 * An obligation attached to a policy decision.
 * Authorization says "you may"; obligations say "you may, under these conditions".
 */
public record Obligation(
        ObligationType type,
        Map<String, Object> parameters
) {
    public Obligation {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public static Obligation of(ObligationType type) {
        return new Obligation(type, Map.of());
    }

    public static Obligation audit() {
        return of(ObligationType.AUDIT);
    }

    public static Obligation hitl() {
        return of(ObligationType.HITL);
    }
}
