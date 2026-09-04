package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionVote;

import java.util.List;

public interface KnowledgeAnswerResolutionJointQuorumEvaluator {

    KnowledgeAnswerResolutionJointQuorumDecision evaluate(
            KnowledgeAnswerResolutionJointMembership membership,
            List<KnowledgeAnswerResolutionVote> votes
    );
}
