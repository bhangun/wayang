package tech.kayys.wayang.knowledge.exchange.contradiction;

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

public final class Sha256KnowledgeVerifiedAnswerArtifactFingerprinter
        implements KnowledgeVerifiedAnswerArtifactFingerprinter {

    private final KnowledgeVerifiedAnswerArtifactCanonicalizer
            canonicalizer;

    public Sha256KnowledgeVerifiedAnswerArtifactFingerprinter() {
        this(
                new DefaultKnowledgeVerifiedAnswerArtifactCanonicalizer()
        );
    }

    public Sha256KnowledgeVerifiedAnswerArtifactFingerprinter(
            KnowledgeVerifiedAnswerArtifactCanonicalizer canonicalizer) {

        this.canonicalizer = canonicalizer;
    }

    @Override
    public String fingerprint(
            KnowledgeVerifiedAnswerArtifact artifact) {

        try {

            byte[] digest =
                    MessageDigest
                            .getInstance("SHA-256")
                            .digest(
                                    canonicalizer
                                            .canonicalize(artifact)
                                            .getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                            );

            StringBuilder result =
                    new StringBuilder("sha256:");

            for (byte b : digest) {
                result.append(
                        String.format(
                                "%02x",
                                b & 0xff
                        )
                );
            }

            return result.toString();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to fingerprint answer artifact",
                    e
            );
        }
    }
}
