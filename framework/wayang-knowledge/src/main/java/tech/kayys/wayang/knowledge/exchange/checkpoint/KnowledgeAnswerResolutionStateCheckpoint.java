package tech.kayys.wayang.knowledge.exchange.checkpoint;

import tech.kayys.wayang.knowledge.exchange.statemachine.KnowledgeAnswerResolutionState;

import java.time.Instant;
import java.util.Objects;

public record KnowledgeAnswerResolutionStateCheckpoint(
        long lastAppliedIndex,
        long term,
        String epochId,
        String stateFingerprint,
        KnowledgeAnswerResolutionState state,
        Instant createdAt,
        KnowledgeAnswerResolutionCheckpointStatus status
) {
    public KnowledgeAnswerResolutionStateCheckpoint {
        Objects.requireNonNull(stateFingerprint, "stateFingerprint");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(status, "status");

        if (lastAppliedIndex < -1) {
            throw new IllegalArgumentException("lastAppliedIndex must be >= -1");
        }
        if (term < 0) {
            throw new IllegalArgumentException("term must be >= 0");
        }
    }
}
