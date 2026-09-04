package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusEpoch;
import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusProposal;

public interface KnowledgeAnswerResolutionConsensusMembershipValidator {

    boolean validate(
            KnowledgeAnswerResolutionConsensusEpoch epoch,
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionMembershipSet membershipSet
    );
}
