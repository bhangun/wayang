package tech.kayys.wayang.knowledge.exchange.compact;

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

public final class KnowledgeAnswerArtifactGraphEngine {

    private final KnowledgeAnswerArtifactSimilarityService similarity;

    private final KnowledgeAnswerArtifactEquivalencePolicy
            equivalencePolicy;

    private final KnowledgeAnswerArtifactConflictDetector
            conflictDetector;

    private final KnowledgeAnswerArtifactGraphStore graphStore;

    public KnowledgeAnswerArtifactGraphEngine(
            KnowledgeAnswerArtifactSimilarityService similarity,
            KnowledgeAnswerArtifactEquivalencePolicy equivalencePolicy,
            KnowledgeAnswerArtifactConflictDetector conflictDetector,
            KnowledgeAnswerArtifactGraphStore graphStore) {

        this.similarity = similarity;
        this.equivalencePolicy = equivalencePolicy;
        this.conflictDetector = conflictDetector;
        this.graphStore = graphStore;
    }

    public KnowledgeAnswerArtifactGraph build(
            List<KnowledgeVerifiedAnswerArtifact> artifacts) {

        List<KnowledgeAnswerArtifactRelation> relations =
                new ArrayList<>();

        for (int i = 0; i < artifacts.size(); i++) {

            for (int j = i + 1;
                    j < artifacts.size();
                    j++) {

                var left =
                        artifacts.get(i);

                var right =
                        artifacts.get(j);

                double similarityScore =
                        compareSimilarity(left, right);

                var relation =
                        classify(
                                left,
                                right,
                                similarityScore
                        );

                if (relation != null) {

                    graphStore.addRelation(relation);
                    relations.add(relation);
                }
            }
        }

        List<KnowledgeAnswerArtifactCandidate>
                candidates =
                artifacts.stream()
                        .map(this::candidate)
                        .toList();

        return new KnowledgeAnswerArtifactGraph(
                java.util.UUID.randomUUID()
                        .toString(),
                candidates,
                relations,
                java.util.Map.of()
        );
    }

    private KnowledgeAnswerArtifactRelation classify(
            KnowledgeVerifiedAnswerArtifact left,
            KnowledgeVerifiedAnswerArtifact right,
            double similarityScore) {

        var conflict =
                conflictDetector.detect(
                        left,
                        right
                );

        if (conflict.type()
                != KnowledgeAnswerArtifactConflictType.NONE) {

            return relation(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactRelationType
                            .CONTRADICTS,
                    conflict.confidence(),
                    conflict.reason()
            );
        }

        if (similarityScore >= 0.90) {

            return relation(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactRelationType
                            .NEAR_DUPLICATE,
                    similarityScore,
                    "Highly similar verified answers"
            );
        }

        if (similarityScore >= 0.65) {

            return relation(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactRelationType
                            .RELATED,
                    similarityScore,
                    "Related answer artifacts"
            );
        }

        return null;
    }

    private KnowledgeAnswerArtifactRelation relation(
            String left,
            String right,
            KnowledgeAnswerArtifactRelationType type,
            double confidence,
            String reason) {

        return new KnowledgeAnswerArtifactRelation(
                java.util.UUID.randomUUID()
                        .toString(),
                left,
                right,
                type,
                confidence,
                reason,
                Instant.now(),
                java.util.Map.of()
        );
    }

    private double compareSimilarity(
            KnowledgeVerifiedAnswerArtifact left,
            KnowledgeVerifiedAnswerArtifact right) {

        var leftTerms =
                left.response()
                        .claims()
                        .stream()
                        .map(c ->
                                c.text()
                                        .toLowerCase())
                        .collect(
                                java.util.stream.Collectors
                                        .toSet()
                        );

        var rightTerms =
                right.response()
                        .claims()
                        .stream()
                        .map(c ->
                                c.text()
                                        .toLowerCase())
                        .collect(
                                java.util.stream.Collectors
                                        .toSet()
                        );

        if (leftTerms.isEmpty()
                || rightTerms.isEmpty()) {

            return 0.0;
        }

        var intersection =
                new java.util.HashSet<>(leftTerms);

        intersection.retainAll(rightTerms);

        var union =
                new java.util.HashSet<>(leftTerms);

        union.addAll(rightTerms);

        return (double) intersection.size()
                / union.size();
    }

    private KnowledgeAnswerArtifactCandidate candidate(
            KnowledgeVerifiedAnswerArtifact artifact) {

        return new KnowledgeAnswerArtifactCandidate(
                artifact.artifactId(),
                artifact.responseId(),
                "local",
                artifact.tenantId(),
                artifact.workspaceId(),
                artifact.projectId(),
                artifact.agentId(),
                1.0,
                0.5,
                1.0,
                1.0,
                1.0,
                1.0,
                true,
                false,
                true,
                java.util.Map.of(
                        "responseFingerprint",
                        artifact.responseFingerprint()
                )
        );
    }
}
