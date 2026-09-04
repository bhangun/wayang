package tech.kayys.wayang.knowledge.exchange.membership;

import java.time.Instant;
import java.util.List;

public record KnowledgeAnswerResolutionMembershipTransitionResult(
        KnowledgeAnswerResolutionMembershipTransitionState state,
        String transitionId,
        String oldEpochId,
        String newEpochId,
        boolean oldQuorumReached,
        boolean newQuorumReached,
        boolean safeToFinalize,
        Instant decidedAt,
        List<String> diagnostics
) {
    public KnowledgeAnswerResolutionMembershipTransitionResult {
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }
}
