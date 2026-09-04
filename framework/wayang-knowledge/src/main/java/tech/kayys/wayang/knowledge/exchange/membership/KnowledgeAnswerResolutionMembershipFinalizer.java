package tech.kayys.wayang.knowledge.exchange.membership;

public interface KnowledgeAnswerResolutionMembershipFinalizer {

    KnowledgeAnswerResolutionMembershipTransitionResult finalizeTransition(
            KnowledgeAnswerResolutionMembershipTransition transition,
            KnowledgeAnswerResolutionMembershipTransitionResult result
    );
}
