package tech.kayys.wayang.knowledge.exchange.membership;

import java.time.Instant;
import java.util.Objects;

public record KnowledgeAnswerResolutionMembership(
        String runtimeId,
        String tenantId,
        String workspaceId,
        String projectId,
        KnowledgeAnswerResolutionMembershipState state,
        Instant effectiveFrom,
        Instant effectiveUntil,
        String metadata
) {
    public KnowledgeAnswerResolutionMembership {
        Objects.requireNonNull(runtimeId, "runtimeId");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom");
    }

    public boolean activeAt(Instant instant) {
        return state == KnowledgeAnswerResolutionMembershipState.ACTIVE
                && !instant.isBefore(effectiveFrom)
                && (effectiveUntil == null || instant.isBefore(effectiveUntil));
    }
}
