package tech.kayys.wayang.knowledge.exchange.membership;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record KnowledgeAnswerResolutionJointMembership(
        String transitionId,
        String oldEpochId,
        String newEpochId,
        List<String> oldRuntimeIds,
        List<String> newRuntimeIds,
        int oldQuorum,
        int newQuorum,
        Instant effectiveFrom,
        Instant effectiveUntil
) {
    public KnowledgeAnswerResolutionJointMembership {
        Objects.requireNonNull(transitionId, "transitionId");
        Objects.requireNonNull(oldEpochId, "oldEpochId");
        Objects.requireNonNull(newEpochId, "newEpochId");
        Objects.requireNonNull(oldRuntimeIds, "oldRuntimeIds");
        Objects.requireNonNull(newRuntimeIds, "newRuntimeIds");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom");

        oldRuntimeIds = List.copyOf(oldRuntimeIds);
        newRuntimeIds = List.copyOf(newRuntimeIds);

        if (oldQuorum < 1 || newQuorum < 1) {
            throw new IllegalArgumentException("quorum must be positive");
        }

        if (oldQuorum > oldRuntimeIds.size() || newQuorum > newRuntimeIds.size()) {
            throw new IllegalArgumentException("quorum exceeds membership size");
        }
    }

    public boolean activeAt(Instant instant) {
        return !instant.isBefore(effectiveFrom)
                && (effectiveUntil == null || instant.isBefore(effectiveUntil));
    }

    public boolean contains(String runtimeId) {
        return oldRuntimeIds.contains(runtimeId) || newRuntimeIds.contains(runtimeId);
    }
}
