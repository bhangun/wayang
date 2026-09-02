package tech.kayys.wayang.knowledge.exchange.resolution;

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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KnowledgeAnswerResolutionEngine {

    private final KnowledgeAnswerResolutionPolicy policy;

    private final KnowledgeAnswerEligibilityService eligibility;

    private final KnowledgeAnswerAuthorityResolver authority;

    private final KnowledgeAnswerTrustResolver trust;

    private final KnowledgeAnswerProvenanceScorer provenance;

    private final KnowledgeAnswerScopeScorer scope;

    private final KnowledgeAnswerSupportScorer support;

    private final KnowledgeAnswerResolutionScorer scorer;

    public KnowledgeAnswerResolutionEngine(
            KnowledgeAnswerResolutionPolicy policy) {

        this(
                policy,
                new DefaultKnowledgeAnswerEligibilityService(),
                new DefaultKnowledgeAnswerAuthorityResolver(),
                new DefaultKnowledgeAnswerTrustResolver(),
                new DefaultKnowledgeAnswerProvenanceScorer(),
                new DefaultKnowledgeAnswerScopeScorer(),
                new DefaultKnowledgeAnswerSupportScorer(),
                new KnowledgeAnswerResolutionScorer()
        );
    }

    public KnowledgeAnswerResolutionEngine(
            KnowledgeAnswerResolutionPolicy policy,
            KnowledgeAnswerEligibilityService eligibility,
            KnowledgeAnswerAuthorityResolver authority,
            KnowledgeAnswerTrustResolver trust,
            KnowledgeAnswerProvenanceScorer provenance,
            KnowledgeAnswerScopeScorer scope,
            KnowledgeAnswerSupportScorer support,
            KnowledgeAnswerResolutionScorer scorer) {

        this.policy = policy;
        this.eligibility = eligibility;
        this.authority = authority;
        this.trust = trust;
        this.provenance = provenance;
        this.scope = scope;
        this.support = support;
        this.scorer = scorer;
    }

    public KnowledgeAnswerResolutionResult resolve(
            List<KnowledgeVerifiedAnswerArtifact> artifacts,
            List<KnowledgeAnswerArtifactRelation> relations,
            KnowledgeAnswerResolutionContext context) {

        List<KnowledgeAnswerResolutionCandidate>
                candidates = new ArrayList<>();

        for (var artifact : artifacts) {

            candidates.add(
                    scorer.score(
                            artifact,
                            context,
                            relations,
                            eligibility,
                            authority,
                            trust,
                            provenance,
                            scope,
                            support
                    )
            );
        }

        candidates.sort(
                Comparator.comparingDouble(
                        KnowledgeAnswerResolutionCandidate
                                ::finalScore
                ).reversed()
        );

        var decision =
                policy.decide(
                        candidates,
                        relations,
                        context
                );

        return buildResult(
                candidates,
                relations,
                decision
        );
    }

    private KnowledgeAnswerResolutionResult buildResult(
            List<KnowledgeAnswerResolutionCandidate> candidates,
            List<KnowledgeAnswerArtifactRelation> relations,
            KnowledgeAnswerResolutionDecision decision) {

        List<String> conflicts =
                relations.stream()
                        .filter(r ->
                                r.type()
                                        == KnowledgeAnswerArtifactRelationType
                                        .CONTRADICTS)
                        .flatMap(r ->
                                java.util.stream.Stream.of(
                                        r.sourceArtifactId(),
                                        r.targetArtifactId()
                                ))
                        .distinct()
                        .toList();

        String preferred =
                candidates.stream()
                        .filter(
                                KnowledgeAnswerResolutionCandidate
                                        ::eligible)
                        .findFirst()
                        .map(
                                KnowledgeAnswerResolutionCandidate
                                        ::artifactId
                        )
                        .orElse(null);

        KnowledgeAnswerResolutionStatus status;

        List<String> accepted =
                new ArrayList<>();

        List<String> rejected =
                new ArrayList<>();

        switch (decision) {

            case ACCEPT -> {
                status =
                        KnowledgeAnswerResolutionStatus
                                .CONSENSUS;

                candidates.stream()
                        .filter(
                                KnowledgeAnswerResolutionCandidate
                                        ::eligible)
                        .forEach(c ->
                                accepted.add(
                                        c.artifactId()
                                ));
            }

            case PREFER -> {
                status =
                        KnowledgeAnswerResolutionStatus
                                .PREFERRED;

                if (preferred != null) {
                    accepted.add(preferred);
                }

                candidates.stream()
                        .filter(c ->
                                !c.artifactId()
                                        .equals(preferred))
                        .forEach(c ->
                                rejected.add(
                                        c.artifactId()
                                ));
            }

            case KEEP_MULTIPLE -> {
                status =
                        conflicts.isEmpty()
                                ? KnowledgeAnswerResolutionStatus
                                        .AMBIGUOUS
                                : KnowledgeAnswerResolutionStatus
                                        .CONFLICTED;

                accepted.addAll(conflicts);
            }

            case REJECT -> {
                status =
                        KnowledgeAnswerResolutionStatus
                                .BLOCKED;

                candidates.stream()
                        .map(
                                KnowledgeAnswerResolutionCandidate
                                        ::artifactId)
                        .forEach(rejected::add);
            }

            case INSUFFICIENT_EVIDENCE -> {
                status =
                        KnowledgeAnswerResolutionStatus
                                .INSUFFICIENT;
            }

            default -> {
                status =
                        KnowledgeAnswerResolutionStatus
                                .FAILED;
            }
        }

        double confidence =
                candidates.isEmpty()
                        ? 0.0
                        : candidates.get(0)
                                .finalScore();

        return new KnowledgeAnswerResolutionResult(
                java.util.UUID.randomUUID()
                        .toString(),
                status,
                decision,
                preferred,
                accepted,
                rejected,
                conflicts,
                confidence,
                candidates,
                relations,
                java.util.Map.of()
        );
    }
}
