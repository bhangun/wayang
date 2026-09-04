package tech.kayys.wayang.knowledge.exchange.membership;

import java.time.Instant;
import java.util.List;

public record KnowledgeAnswerResolutionMembershipTransition(
        String transitionId,
        KnowledgeAnswerResolutionMembershipChangeType type,
        String oldEpochId,
        String proposedEpochId,
        List<String> oldRuntimeIds,
        List<String> newRuntimeIds,
        int oldQuorum,
        int newQuorum,
        String proposerRuntimeId,
        Instant proposedAt,
        Instant effectiveAt,
        String reason,
        String metadata
) {
    public KnowledgeAnswerResolutionMembershipTransition {
        oldRuntimeIds = oldRuntimeIds == null ? List.of() : List.copyOf(oldRuntimeIds);
        newRuntimeIds = newRuntimeIds == null ? List.of() : List.copyOf(newRuntimeIds);
    }
}
