package tech.kayys.wayang.security.delegation;

import java.util.List;

/**
 * Identifies target agents or services authorized to receive delegated authority.
 */
public record DelegationAudience(List<String> targetAgentIds) {

    public DelegationAudience {
        targetAgentIds = targetAgentIds == null ? List.of() : List.copyOf(targetAgentIds);
    }

    public static DelegationAudience of(String... agentIds) {
        return new DelegationAudience(List.of(agentIds));
    }

    public static DelegationAudience any() {
        return new DelegationAudience(List.of("*"));
    }

    public boolean allows(String agentId) {
        return targetAgentIds.contains("*") || targetAgentIds.contains(agentId);
    }
}
