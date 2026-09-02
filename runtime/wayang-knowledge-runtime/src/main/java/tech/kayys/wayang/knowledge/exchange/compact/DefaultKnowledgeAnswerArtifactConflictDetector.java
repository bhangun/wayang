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


import java.util.HashMap;

public final class DefaultKnowledgeAnswerArtifactConflictDetector
        implements KnowledgeAnswerArtifactConflictDetector {

    @Override
    public KnowledgeAnswerArtifactConflict detect(
            KnowledgeVerifiedAnswerArtifact left,
            KnowledgeVerifiedAnswerArtifact right) {

        var leftClaims =
                left.response().claims();

        var rightClaims =
                right.response().claims();

        var rightByText =
                new HashMap<String,
                        KnowledgeVerifiedClaim>();

        for (var claim : rightClaims) {

            rightByText.put(
                    normalize(claim.text()),
                    claim
            );
        }

        for (var claim : leftClaims) {

            var opposite =
                    rightByText.get(
                            normalize(claim.text())
                    );

            if (opposite == null) {
                continue;
            }

            if (claim.status()
                    != opposite.status()) {

                return new KnowledgeAnswerArtifactConflict(
                        java.util.UUID.randomUUID()
                                .toString(),
                        left.artifactId(),
                        right.artifactId(),
                        KnowledgeAnswerArtifactConflictType
                                .CLAIM_CONFLICT,
                        0.85,
                        "Same normalized claim has different verification status",
                        java.util.Map.of()
                );
            }
        }

        return new KnowledgeAnswerArtifactConflict(
                java.util.UUID.randomUUID()
                        .toString(),
                left.artifactId(),
                right.artifactId(),
                KnowledgeAnswerArtifactConflictType
                        .NONE,
                0.0,
                "No detectable conflict",
                java.util.Map.of()
        );
    }

    private String normalize(String text) {

        return text == null
                ? ""
                : text.toLowerCase()
                    .replaceAll("\\s+", " ")
                    .trim();
    }
}
