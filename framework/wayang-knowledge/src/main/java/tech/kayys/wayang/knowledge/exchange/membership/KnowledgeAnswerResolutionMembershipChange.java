package tech.kayys.wayang.knowledge.exchange.membership;

import java.time.Instant;
import java.util.List;

public record KnowledgeAnswerResolutionMembershipChange(
        String changeId,
        String currentEpochId,
        KnowledgeAnswerResolutionMembershipChangeType type,
        List<String> currentRuntimeIds,
        List<String> proposedRuntimeIds,
        int proposedQuorum,
        String proposerRuntimeId,
        Instant proposedAt,
        Instant effectiveAt,
        String reason,
        String metadata
) {
    public KnowledgeAnswerResolutionMembershipChange {
        currentRuntimeIds = currentRuntimeIds == null ? List.of() : List.copyOf(currentRuntimeIds);
        proposedRuntimeIds = proposedRuntimeIds == null ? List.of() : List.copyOf(proposedRuntimeIds);
    }
}
