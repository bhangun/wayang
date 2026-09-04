package tech.kayys.wayang.knowledge.exchange.membership;

public interface KnowledgeAnswerResolutionEpochFinalizationGuard {
    boolean canFinalize(KnowledgeAnswerResolutionMembershipTransitionResult result);
}
