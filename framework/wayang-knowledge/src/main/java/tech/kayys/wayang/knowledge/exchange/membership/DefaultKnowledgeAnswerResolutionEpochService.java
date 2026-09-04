package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusEpoch;

import java.time.Instant;
import java.util.UUID;

public final class DefaultKnowledgeAnswerResolutionEpochService
        implements KnowledgeAnswerResolutionEpochService {

    @Override
    public KnowledgeAnswerResolutionConsensusEpoch create(
            KnowledgeAnswerResolutionMembershipSet membershipSet,
            KnowledgeAnswerResolutionConsensusEpoch previous,
            Instant now) {

        return new KnowledgeAnswerResolutionConsensusEpoch(
                UUID.randomUUID().toString(),
                previous == null ? 0 : previous.sequence() + 1,
                membershipSet.participantSetFingerprint(),
                previous == null ? null : previous.epochId(),
                now,
                membershipSet.effectiveFrom(),
                membershipSet.effectiveUntil()
        );
    }

    @Override
    public boolean validate(
            KnowledgeAnswerResolutionConsensusEpoch epoch,
            KnowledgeAnswerResolutionMembershipSet membershipSet,
            Instant now) {

        if (epoch == null || membershipSet == null) {
            return false;
        }

        return epoch.participantSetFingerprint().equals(membershipSet.participantSetFingerprint())
                && epoch.effectiveAt(now);
    }
}
