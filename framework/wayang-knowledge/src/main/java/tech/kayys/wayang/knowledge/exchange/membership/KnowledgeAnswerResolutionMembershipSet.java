package tech.kayys.wayang.knowledge.exchange.membership;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record KnowledgeAnswerResolutionMembershipSet(
        String epochId,
        long sequence,
        List<KnowledgeAnswerResolutionMembership> members,
        int quorum,
        String participantSetFingerprint,
        Instant effectiveFrom,
        Instant effectiveUntil
) {
    public KnowledgeAnswerResolutionMembershipSet {
        Objects.requireNonNull(epochId, "epochId");
        Objects.requireNonNull(members, "members");
        Objects.requireNonNull(participantSetFingerprint, "participantSetFingerprint");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom");

        members = List.copyOf(members);

        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must be >= 0");
        }
        if (quorum < 1) {
            throw new IllegalArgumentException("quorum must be >= 1");
        }
        if (quorum > members.size()) {
            throw new IllegalArgumentException("quorum cannot exceed member count");
        }
    }

    public boolean activeAt(Instant instant) {
        return !instant.isBefore(effectiveFrom)
                && (effectiveUntil == null || instant.isBefore(effectiveUntil));
    }
}
