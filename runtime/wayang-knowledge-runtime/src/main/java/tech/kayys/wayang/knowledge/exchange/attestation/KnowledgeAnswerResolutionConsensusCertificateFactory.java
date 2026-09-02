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


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

public final class
KnowledgeAnswerResolutionConsensusCertificateFactory {

    public KnowledgeAnswerResolutionConsensusCertificate create(
            KnowledgeAnswerResolutionConsensusProposal proposal,
            KnowledgeAnswerResolutionConsensusResult result,
            List<String> agreeingRuntimeIds,
            Instant now) {

        if (result.status()
                != KnowledgeAnswerResolutionConsensusStatus
                        .CONSENSUS_REACHED) {

            throw new IllegalArgumentException(
                    "Cannot create certificate without consensus"
            );
        }

        String canonical =
                proposal.consensusId()
                + "|"
                + proposal.keyFingerprint()
                + "|"
                + result.winningResolutionFingerprint()
                + "|"
                + result.winningDependencyFingerprint()
                + "|"
                + String.join(
                        ",",
                        agreeingRuntimeIds
                );

        String fingerprint =
                sha256(canonical);

        return new KnowledgeAnswerResolutionConsensusCertificate(
                java.util.UUID.randomUUID().toString(),
                proposal.consensusId(),
                proposal.keyFingerprint(),
                result.winningResolutionFingerprint(),
                result.winningDependencyFingerprint(),
                proposal.participantRuntimeIds(),
                agreeingRuntimeIds,
                result.requiredVotes(),
                now,
                proposal.expiresAt(),
                fingerprint,
                java.util.Map.of()
        );
    }

    private String sha256(String value) {

        try {

            byte[] digest =
                    MessageDigest
                            .getInstance("SHA-256")
                            .digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            StringBuilder builder =
                    new StringBuilder();

            for (byte b : digest) {

                builder.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return "sha256:" + builder;

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint consensus certificate",
                    e
            );
        }
    }
}
