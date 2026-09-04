package tech.kayys.wayang.knowledge.exchange.membership;

import tech.kayys.wayang.knowledge.exchange.attestation.KnowledgeAnswerResolutionVote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DefaultKnowledgeAnswerResolutionJointQuorumEvaluator
        implements KnowledgeAnswerResolutionJointQuorumEvaluator {

    @Override
    public KnowledgeAnswerResolutionJointQuorumDecision evaluate(
            KnowledgeAnswerResolutionJointMembership membership,
            List<KnowledgeAnswerResolutionVote> votes) {

        if (membership == null || votes == null) {
            return new KnowledgeAnswerResolutionJointQuorumDecision(
                    false, false, 0, 0,
                    membership != null ? membership.oldQuorum() : 0,
                    membership != null ? membership.newQuorum() : 0,
                    List.of(), List.of());
        }

        Set<String> oldMembers = new HashSet<>(membership.oldRuntimeIds());
        Set<String> newMembers = new HashSet<>(membership.newRuntimeIds());

        List<String> oldAgreeing = new ArrayList<>();
        List<String> newAgreeing = new ArrayList<>();

        for (KnowledgeAnswerResolutionVote vote : votes) {
            if (!vote.eligible() || !vote.verified()) {
                continue;
            }

            if (oldMembers.contains(vote.runtimeId())) {
                oldAgreeing.add(vote.runtimeId());
            }
            if (newMembers.contains(vote.runtimeId())) {
                newAgreeing.add(vote.runtimeId());
            }
        }

        int oldVotes = new HashSet<>(oldAgreeing).size();
        int newVotes = new HashSet<>(newAgreeing).size();

        return new KnowledgeAnswerResolutionJointQuorumDecision(
                oldVotes >= membership.oldQuorum(),
                newVotes >= membership.newQuorum(),
                oldVotes,
                newVotes,
                membership.oldQuorum(),
                membership.newQuorum(),
                List.copyOf(new HashSet<>(oldAgreeing)),
                List.copyOf(new HashSet<>(newAgreeing))
        );
    }
}
