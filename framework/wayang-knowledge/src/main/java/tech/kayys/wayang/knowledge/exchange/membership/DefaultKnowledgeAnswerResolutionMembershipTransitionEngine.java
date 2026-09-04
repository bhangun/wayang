package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionVote;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class DefaultKnowledgeAnswerResolutionMembershipTransitionEngine
        implements KnowledgeAnswerResolutionMembershipTransitionEngine {

    private final KnowledgeAnswerResolutionJointQuorumEvaluator quorumEvaluator;

    public DefaultKnowledgeAnswerResolutionMembershipTransitionEngine(
            KnowledgeAnswerResolutionJointQuorumEvaluator quorumEvaluator) {
        this.quorumEvaluator = quorumEvaluator != null
                ? quorumEvaluator
                : new DefaultKnowledgeAnswerResolutionJointQuorumEvaluator();
    }

    public DefaultKnowledgeAnswerResolutionMembershipTransitionEngine() {
        this(new DefaultKnowledgeAnswerResolutionJointQuorumEvaluator());
    }

    @Override
    public KnowledgeAnswerResolutionMembershipTransitionResult evaluate(
            KnowledgeAnswerResolutionMembershipTransition transition,
            KnowledgeAnswerResolutionJointMembership jointMembership,
            List<KnowledgeAnswerResolutionVote> votes) {

        List<String> diagnostics = new ArrayList<>();

        if (transition == null || jointMembership == null) {
            diagnostics.add("Transition or joint membership is null");
            return new KnowledgeAnswerResolutionMembershipTransitionResult(
                    KnowledgeAnswerResolutionMembershipTransitionState.ABORTED,
                    transition != null ? transition.transitionId() : "unknown",
                    transition != null ? transition.oldEpochId() : "unknown",
                    transition != null ? transition.proposedEpochId() : "unknown",
                    false, false, false, Instant.now(), diagnostics
            );
        }

        if (!transition.oldEpochId().equals(jointMembership.oldEpochId())) {
            diagnostics.add("Old epoch does not match joint configuration");
            return result(transition, KnowledgeAnswerResolutionMembershipTransitionState.ABORTED, false, false, false, diagnostics);
        }

        if (!transition.proposedEpochId().equals(jointMembership.newEpochId())) {
            diagnostics.add("New epoch does not match joint configuration");
            return result(transition, KnowledgeAnswerResolutionMembershipTransitionState.ABORTED, false, false, false, diagnostics);
        }

        KnowledgeAnswerResolutionJointQuorumDecision quorum = quorumEvaluator.evaluate(jointMembership, votes);

        if (!quorum.oldQuorumReached()) {
            diagnostics.add("Old configuration quorum not reached");
        }

        if (!quorum.newQuorumReached()) {
            diagnostics.add("New configuration quorum not reached");
        }

        if (!quorum.reached()) {
            return result(transition, KnowledgeAnswerResolutionMembershipTransitionState.JOINT,
                    quorum.oldQuorumReached(), quorum.newQuorumReached(), false, diagnostics);
        }

        diagnostics.add("Joint quorum reached");
        return result(transition, KnowledgeAnswerResolutionMembershipTransitionState.FINALIZING,
                true, true, true, diagnostics);
    }

    private KnowledgeAnswerResolutionMembershipTransitionResult result(
            KnowledgeAnswerResolutionMembershipTransition transition,
            KnowledgeAnswerResolutionMembershipTransitionState state,
            boolean oldQuorum,
            boolean newQuorum,
            boolean safe,
            List<String> diagnostics) {

        return new KnowledgeAnswerResolutionMembershipTransitionResult(
                state,
                transition.transitionId(),
                transition.oldEpochId(),
                transition.proposedEpochId(),
                oldQuorum,
                newQuorum,
                safe,
                Instant.now(),
                diagnostics
        );
    }
}
