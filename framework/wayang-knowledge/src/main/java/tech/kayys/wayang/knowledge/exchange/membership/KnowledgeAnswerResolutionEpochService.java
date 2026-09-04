package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusEpoch;

import java.time.Instant;

public interface KnowledgeAnswerResolutionEpochService {

    KnowledgeAnswerResolutionConsensusEpoch create(
            KnowledgeAnswerResolutionMembershipSet membershipSet,
            KnowledgeAnswerResolutionConsensusEpoch previous,
            Instant now
    );

    boolean validate(
            KnowledgeAnswerResolutionConsensusEpoch epoch,
            KnowledgeAnswerResolutionMembershipSet membershipSet,
            Instant now
    );
}
