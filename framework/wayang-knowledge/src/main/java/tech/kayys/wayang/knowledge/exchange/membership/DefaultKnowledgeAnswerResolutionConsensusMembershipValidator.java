package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusEpoch;
import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionConsensusProposal;

public final class DefaultKnowledgeAnswerResolutionConsensusMembershipValidator
        implements KnowledgeAnswerResolutionConsensusMembershipValidator {

    @Override
    public boolean validate(
            KnowledgeAnswerResolutionConsensusEpoch epoch,
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionMembershipSet membershipSet) {

        if (epoch == null || proposal == null || membershipSet == null) {
            return false;
        }

        // Validate proposal's epochId or consensusId matches epoch
        String proposalEpoch = proposal.epochId() != null ? proposal.epochId() : proposal.consensusId();
        if (!epoch.epochId().equals(proposalEpoch)) {
            return false;
        }

        return epoch.participantSetFingerprint().equals(membershipSet.participantSetFingerprint());
    }
}
