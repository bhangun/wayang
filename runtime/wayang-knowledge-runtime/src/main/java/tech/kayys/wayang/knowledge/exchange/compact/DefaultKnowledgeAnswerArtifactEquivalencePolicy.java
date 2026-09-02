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


public final class DefaultKnowledgeAnswerArtifactEquivalencePolicy
        implements KnowledgeAnswerArtifactEquivalencePolicy {

    @Override
    public KnowledgeAnswerArtifactComparison compare(
            KnowledgeAnswerArtifactIndexEntry left,
            KnowledgeAnswerArtifactIndexEntry right,
            double similarity) {

        if (left.artifactId()
                .equals(right.artifactId())) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .IDENTICAL,
                    1.0,
                    1.0,
                    "Same artifact identity",
                    java.util.Map.of()
            );
        }

        if (left.responseFingerprint() != null
                && left.responseFingerprint()
                        .equals(right.responseFingerprint())) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .DUPLICATE,
                    1.0,
                    1.0,
                    "Same response fingerprint",
                    java.util.Map.of()
            );
        }

        if (similarity >= 0.90) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .NEAR_DUPLICATE,
                    similarity,
                    0.80,
                    "Highly similar indexed concepts",
                    java.util.Map.of()
            );
        }

        if (similarity >= 0.65) {

            return new KnowledgeAnswerArtifactComparison(
                    left.artifactId(),
                    right.artifactId(),
                    KnowledgeAnswerArtifactComparisonType
                            .RELATED,
                    similarity,
                    0.50,
                    "Related indexed concepts",
                    java.util.Map.of()
            );
        }

        return new KnowledgeAnswerArtifactComparison(
                left.artifactId(),
                right.artifactId(),
                KnowledgeAnswerArtifactComparisonType
                        .UNKNOWN,
                similarity,
                0.10,
                "Insufficient evidence for equivalence",
                java.util.Map.of()
        );
    }
}
