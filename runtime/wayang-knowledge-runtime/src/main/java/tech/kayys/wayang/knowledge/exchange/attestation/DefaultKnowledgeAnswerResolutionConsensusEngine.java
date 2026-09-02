package tech.kayys.wayang.knowledge.exchange.attestation;

import tech.kayys.wayang.knowledge.*;
import tech.kayys.wayang.knowledge.seal.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.pack.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.*;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;
import tech.kayys.wayang.knowledge.exchange.*;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.exchange.session.*;
import tech.kayys.wayang.knowledge.exchange.binding.*;
import tech.kayys.wayang.knowledge.exchange.envelope.*;
import tech.kayys.wayang.knowledge.exchange.trust.*;
import tech.kayys.wayang.knowledge.exchange.identity.*;
import tech.kayys.wayang.knowledge.exchange.capability.*;
import tech.kayys.wayang.knowledge.exchange.protocol.*;
import tech.kayys.wayang.knowledge.exchange.transport.*;
import tech.kayys.wayang.knowledge.exchange.framing.*;
import tech.kayys.wayang.knowledge.exchange.transfer.*;
import tech.kayys.wayang.knowledge.exchange.replication.*;
import tech.kayys.wayang.knowledge.exchange.sync.*;
import tech.kayys.wayang.knowledge.exchange.federation.*;
import tech.kayys.wayang.knowledge.exchange.routing.*;
import tech.kayys.wayang.knowledge.exchange.fusion.*;
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.contradiction.*;
import tech.kayys.wayang.knowledge.exchange.factuality.*;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;
import tech.kayys.wayang.knowledge.exchange.compact.*;
import tech.kayys.wayang.knowledge.exchange.resolution.*;
import tech.kayys.wayang.knowledge.exchange.quorum.*;
import tech.kayys.wayang.knowledge.exchange.selection.*;
import tech.kayys.wayang.knowledge.exchange.coordination.*;
import tech.kayys.wayang.knowledge.exchange.attestation.*;
import tech.kayys.wayang.knowledge.exchange.proof.*;
import tech.kayys.wayang.knowledge.exchange.validity.*;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class
DefaultKnowledgeAnswerResolutionConsensusEngine {

    private final
    KnowledgeAnswerResolutionConsensusParticipantRegistry
            participants;

    private final KnowledgeAnswerResolutionQuorumCalculator
            quorum;

    private final KnowledgeAnswerResolutionConsensusPolicy
            policy;

    public
    DefaultKnowledgeAnswerResolutionConsensusEngine(
            KnowledgeAnswerResolutionConsensusParticipantRegistry
                    participants,
            KnowledgeAnswerResolutionQuorumCalculator quorum,
            KnowledgeAnswerResolutionConsensusPolicy policy) {

        this.participants = participants;
        this.quorum = quorum;
        this.policy = policy;
    }

    public KnowledgeAnswerResolutionConsensusResult
    evaluate(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            List<KnowledgeAnswerResolutionVote> votes,
            Instant now) {

        if (proposal.expiresAt() != null
                && now.isAfter(proposal.expiresAt())) {

            return result(
                    proposal,
                    KnowledgeAnswerResolutionConsensusStatus.EXPIRED,
                    null,
                    0,
                    0,
                    0,
                    List.of("Consensus proposal expired"),
                    now
            );
        }

        var eligible =
                participants.activeAt(now)
                        .stream()
                        .filter(
                                participant ->
                                        sameTenant(
                                                participant,
                                                proposal
                                        )
                        )
                        .toList();

        if (eligible.size()
                < policy.minimumParticipants()) {

            return result(
                    proposal,
                    KnowledgeAnswerResolutionConsensusStatus
                            .INSUFFICIENT_PARTICIPANTS,
                    null,
                    eligible.size(),
                    0,
                    0,
                    List.of(
                            "Insufficient trusted consensus participants"
                    ),
                    now
            );
        }

        var validVotes =
                votes.stream()
                        .filter(
                                vote ->
                                        vote.eligible()
                                        && sameScope(
                                                vote,
                                                proposal
                                        )
                                        && !vote.expiredAt(now)
                                        && (!policy.requireVerifiedVotes()
                                            || vote.verified())
                        )
                        .toList();

        if (validVotes.isEmpty()) {

            return result(
                    proposal,
                    KnowledgeAnswerResolutionConsensusStatus
                            .NO_CONSENSUS,
                    null,
                    eligible.size(),
                    0,
                    0,
                    List.of("No valid votes"),
                    now
            );
        }

        Map<String, List<KnowledgeAnswerResolutionVote>>
                groups =
                validVotes.stream()
                        .collect(
                                Collectors.groupingBy(
                                        KnowledgeAnswerResolutionVote
                                                ::resolutionFingerprint
                                )
                        );

        var winner =
                groups.entrySet()
                        .stream()
                        .max(
                                java.util.Comparator.comparingInt(
                                        entry ->
                                                entry.getValue()
                                                        .size()
                                )
                        )
                        .orElse(null);

        if (winner == null) {

            return result(
                    proposal,
                    KnowledgeAnswerResolutionConsensusStatus
                            .NO_CONSENSUS,
                    null,
                    eligible.size(),
                    validVotes.size(),
                    0,
                    List.of("No resolution candidate"),
                    now
            );
        }

        int agreeing =
                winner.getValue().size();

        boolean concurrent =
                validVotes.stream()
                        .map(
                                KnowledgeAnswerResolutionVote
                                        ::versionVector
                        )
                        .distinct()
                        .count() > 1;

        if (concurrent
                && policy.rejectConcurrentVersions()) {

            return result(
                    proposal,
                    KnowledgeAnswerResolutionConsensusStatus
                            .PARTITIONED,
                    null,
                    eligible.size(),
                    validVotes.size(),
                    agreeing,
                    List.of(
                            "Concurrent resolution versions detected"
                    ),
                    now
            );
        }

        if (!quorum.reached(
                agreeing,
                eligible.size(),
                policy)) {

            return result(
                    proposal,
                    KnowledgeAnswerResolutionConsensusStatus
                            .NO_CONSENSUS,
                    null,
                    eligible.size(),
                    validVotes.size(),
                    agreeing,
                    List.of(
                            "Required quorum not reached"
                    ),
                    now
            );
        }

        String dependencyFingerprint =
                winner.getValue()
                        .get(0)
                        .dependencyFingerprint();

        if (policy.requireDependencyAgreement()) {

            boolean dependencyAgreement =
                    winner.getValue()
                            .stream()
                            .allMatch(
                                    vote ->
                                            java.util.Objects.equals(
                                                    dependencyFingerprint,
                                                    vote.dependencyFingerprint()
                                            )
                            );

            if (!dependencyAgreement) {

                return result(
                        proposal,
                        KnowledgeAnswerResolutionConsensusStatus
                                .NO_CONSENSUS,
                        null,
                        eligible.size(),
                        validVotes.size(),
                        agreeing,
                        List.of(
                                "Resolution agrees but dependency state differs"
                        ),
                        now
                );
            }
        }

        return result(
                proposal,
                KnowledgeAnswerResolutionConsensusStatus
                        .CONSENSUS_REACHED,
                winner.getKey(),
                eligible.size(),
                validVotes.size(),
                agreeing,
                List.of(),
                now,
                dependencyFingerprint
        );
    }

    private boolean sameTenant(
            KnowledgeAnswerResolutionConsensusParticipant participant,
            KnowledgeAnswerResolutionConsensusProposal proposal) {

        return java.util.Objects.equals(
                participant.tenantId(),
                proposal.tenantId()
        );
    }

    private boolean sameScope(
            KnowledgeAnswerResolutionVote vote,
            KnowledgeAnswerResolutionConsensusProposal proposal) {

        return java.util.Objects.equals(
                vote.keyFingerprint(),
                proposal.keyFingerprint()
        )
        && java.util.Objects.equals(
                vote.tenantId(),
                proposal.tenantId()
        )
        && java.util.Objects.equals(
                vote.workspaceId(),
                proposal.workspaceId()
        )
        && java.util.Objects.equals(
                vote.projectId(),
                proposal.projectId()
        );
    }

    private KnowledgeAnswerResolutionConsensusResult result(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionConsensusStatus status,
            String winningFingerprint,
            int eligible,
            int received,
            int agreeing,
            List<String> diagnostics,
            Instant decidedAt) {

        return result(
                proposal,
                status,
                winningFingerprint,
                eligible,
                received,
                agreeing,
                diagnostics,
                decidedAt,
                null
        );
    }

    private KnowledgeAnswerResolutionConsensusResult result(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionConsensusStatus status,
            String winningFingerprint,
            int eligible,
            int received,
            int agreeing,
            List<String> diagnostics,
            Instant decidedAt,
            String dependencyFingerprint) {

        int required =
                quorum.requiredVotes(
                        eligible,
                        policy
                );

        double ratio =
                eligible == 0
                        ? 0
                        : (double) agreeing / eligible;

        return new KnowledgeAnswerResolutionConsensusResult(
                proposal.consensusId(),
                proposal.keyFingerprint(),
                status,
                winningFingerprint,
                dependencyFingerprint,
                eligible,
                received,
                agreeing,
                required,
                ratio,
                List.of(),
                diagnostics,
                decidedAt,
                Map.of()
        );
    }
}
