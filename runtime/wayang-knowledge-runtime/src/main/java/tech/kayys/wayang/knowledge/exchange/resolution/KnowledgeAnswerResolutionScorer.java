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


public final class KnowledgeAnswerResolutionScorer {

    public KnowledgeAnswerResolutionCandidate score(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeAnswerResolutionContext context,
            java.util.List<
                    KnowledgeAnswerArtifactRelation> relations,
            KnowledgeAnswerEligibilityService eligibility,
            KnowledgeAnswerAuthorityResolver authority,
            KnowledgeAnswerTrustResolver trust,
            KnowledgeAnswerProvenanceScorer provenance,
            KnowledgeAnswerScopeScorer scope,
            KnowledgeAnswerSupportScorer support) {

        boolean eligible =
                eligibility.eligible(
                        artifact,
                        context
                );

        double authorityScore =
                authority.score(artifact);

        double trustScore =
                trust.score(artifact);

        double provenanceScore =
                provenance.score(artifact);

        double scopeScore =
                scope.score(
                        artifact,
                        context
                );

        double supportScore =
                support.score(
                        artifact.artifactId(),
                        relations
                );

        double freshnessScore =
                freshness(
                        artifact,
                        context
                );

        double conflictPenalty =
                conflictPenalty(
                        artifact.artifactId(),
                        relations
                );

        double finalScore =
                authorityScore * 0.25
                + trustScore * 0.25
                + provenanceScore * 0.10
                + scopeScore * 0.15
                + supportScore * 0.10
                + freshnessScore * 0.15
                - conflictPenalty;

        if (!eligible) {
            finalScore = 0.0;
        }

        return new KnowledgeAnswerResolutionCandidate(
                artifact.artifactId(),
                "local",
                authorityScore,
                trustScore,
                freshnessScore,
                provenanceScore,
                scopeScore,
                supportScore,
                conflictPenalty,
                Math.max(
                        0.0,
                        Math.min(1.0, finalScore)
                ),
                artifact.response()
                        .status()
                        == KnowledgeVerifiedResponseStatus
                        .VERIFIED,
                eligible,
                java.util.Map.of()
        );
    }

    private double freshness(
            KnowledgeVerifiedAnswerArtifact artifact,
            KnowledgeAnswerResolutionContext context) {

        if (context.effectiveAt() == null) {
            return 0.5;
        }

        if (artifact.createdAt() == null) {
            return 0.5;
        }

        return artifact.createdAt()
                .isAfter(context.effectiveAt())
                ? 0.0
                : 1.0;
    }

    private double conflictPenalty(
            String artifactId,
            java.util.List<
                    KnowledgeAnswerArtifactRelation> relations) {

        return relations.stream()
                .filter(r ->
                        r.type()
                                == KnowledgeAnswerArtifactRelationType
                                .CONTRADICTS)
                .filter(r ->
                        r.sourceArtifactId()
                                .equals(artifactId)
                        || r.targetArtifactId()
                                .equals(artifactId))
                .mapToDouble(r ->
                        0.25 * r.confidence())
                .sum();
    }
}
