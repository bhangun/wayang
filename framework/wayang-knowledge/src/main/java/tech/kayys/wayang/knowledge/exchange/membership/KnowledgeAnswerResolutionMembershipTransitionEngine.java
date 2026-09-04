package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionVote;

import java.util.List;

public interface KnowledgeAnswerResolutionMembershipTransitionEngine {

    KnowledgeAnswerResolutionMembershipTransitionResult evaluate(
            KnowledgeAnswerResolutionMembershipTransition transition,
            KnowledgeAnswerResolutionJointMembership jointMembership,
            List<KnowledgeAnswerResolutionVote> votes
    );
}
