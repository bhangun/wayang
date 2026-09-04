package tech.kayys.wayang.knowledge.exchange.membership;

public final class DefaultKnowledgeAnswerResolutionMembershipFinalizer
        implements KnowledgeAnswerResolutionMembershipFinalizer {

    private final KnowledgeAnswerResolutionEpochFinalizationGuard guard;

    public DefaultKnowledgeAnswerResolutionMembershipFinalizer(
            KnowledgeAnswerResolutionEpochFinalizationGuard guard) {
        this.guard = guard != null ? guard : new DefaultKnowledgeAnswerResolutionEpochFinalizationGuard();
    }

    public DefaultKnowledgeAnswerResolutionMembershipFinalizer() {
        this(new DefaultKnowledgeAnswerResolutionEpochFinalizationGuard());
    }

    @Override
    public KnowledgeAnswerResolutionMembershipTransitionResult finalizeTransition(
            KnowledgeAnswerResolutionMembershipTransition transition,
            KnowledgeAnswerResolutionMembershipTransitionResult result) {

        if (!guard.canFinalize(result)) {
            throw new IllegalStateException("Membership transition is not safe to finalize");
        }

        return new KnowledgeAnswerResolutionMembershipTransitionResult(
                KnowledgeAnswerResolutionMembershipTransitionState.COMPLETED,
                transition.transitionId(),
                transition.oldEpochId(),
                transition.proposedEpochId(),
                result.oldQuorumReached(),
                result.newQuorumReached(),
                true,
                result.decidedAt(),
                result.diagnostics()
        );
    }
}
