package tech.kayys.wayang.knowledge.exchange.membership;

import java.util.List;

public record KnowledgeAnswerResolutionJointQuorumDecision(
        boolean oldQuorumReached,
        boolean newQuorumReached,
        int oldVotes,
        int newVotes,
        int oldRequired,
        int newRequired,
        List<String> oldAgreeingRuntimeIds,
        List<String> newAgreeingRuntimeIds
) {
    public KnowledgeAnswerResolutionJointQuorumDecision {
        oldAgreeingRuntimeIds = oldAgreeingRuntimeIds == null ? List.of() : List.copyOf(oldAgreeingRuntimeIds);
        newAgreeingRuntimeIds = newAgreeingRuntimeIds == null ? List.of() : List.copyOf(newAgreeingRuntimeIds);
    }

    public boolean reached() {
        return oldQuorumReached && newQuorumReached;
    }
}
