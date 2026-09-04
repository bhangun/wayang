package tech.kayys.wayang.knowledge.exchange.statemachine;

import java.util.Map;
import java.util.Set;

public record KnowledgeAnswerResolutionState(
        long lastAppliedIndex,
        long currentTerm,
        String currentEpochId,
        Map<String, KnowledgeAnswerResolutionConsensusState> consensuses,
        Set<String> revokedConsensusIds,
        Set<String> activeLeaseIds,
        Set<String> activeRuntimeIds,
        Map<String, byte[]> entries
) {
    public KnowledgeAnswerResolutionState(
            long lastAppliedIndex,
            long currentTerm,
            String currentEpochId,
            Map<String, KnowledgeAnswerResolutionConsensusState> consensuses,
            Set<String> revokedConsensusIds,
            Set<String> activeLeaseIds,
            Set<String> activeRuntimeIds) {
        this(lastAppliedIndex, currentTerm, currentEpochId, consensuses,
                revokedConsensusIds, activeLeaseIds, activeRuntimeIds, Map.of());
    }

    public KnowledgeAnswerResolutionState {
        consensuses = consensuses == null ? Map.of() : Map.copyOf(consensuses);
        revokedConsensusIds = revokedConsensusIds == null ? Set.of() : Set.copyOf(revokedConsensusIds);
        activeLeaseIds = activeLeaseIds == null ? Set.of() : Set.copyOf(activeLeaseIds);
        activeRuntimeIds = activeRuntimeIds == null ? Set.of() : Set.copyOf(activeRuntimeIds);
        entries = entries == null ? Map.of() : Map.copyOf(entries);
    }

    public static KnowledgeAnswerResolutionState initial() {
        return new KnowledgeAnswerResolutionState(-1, 0, null, Map.of(), Set.of(), Set.of(), Set.of(), Map.of());
    }
}
