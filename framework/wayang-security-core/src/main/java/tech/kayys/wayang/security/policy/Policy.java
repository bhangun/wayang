package tech.kayys.wayang.security.policy;

import java.util.List;
import java.util.Objects;

/**
 * A named collection of {@link PolicyRule}s evaluated in priority order.
 */
public record Policy(
        String id,
        String description,
        List<PolicyRule> rules
) {

    public Policy {
        Objects.requireNonNull(id, "id");
        rules = rules == null ? List.of() : List.copyOf(rules);
    }

    public static Policy of(String id, List<PolicyRule> rules) {
        return new Policy(id, null, rules);
    }
}
