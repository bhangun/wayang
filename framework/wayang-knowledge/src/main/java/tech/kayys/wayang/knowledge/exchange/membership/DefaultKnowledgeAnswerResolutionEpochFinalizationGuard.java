package tech.kayys.wayang.knowledge.exchange.membership;

public final class DefaultKnowledgeAnswerResolutionEpochFinalizationGuard
        implements KnowledgeAnswerResolutionEpochFinalizationGuard {

    @Override
    public boolean canFinalize(KnowledgeAnswerResolutionMembershipTransitionResult result) {
        if (result == null) {
            return false;
        }

        return result.state() == KnowledgeAnswerResolutionMembershipTransitionState.FINALIZING
                && result.oldQuorumReached()
                && result.newQuorumReached()
                && result.safeToFinalize();
    }
}
