package tech.kayys.wayang.knowledge.exchange.sync;

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


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class Sha256KnowledgeEvidenceArtifactInventoryFingerprinter
        implements KnowledgeEvidenceArtifactInventoryFingerprinter {

    @Override
    public String fingerprint(
            KnowledgeEvidenceArtifactInventorySnapshot snapshot
    ) {

        var entries =
                snapshot.entries()
                        .stream()
                        .sorted(
                                java.util.Comparator.comparing(
                                        KnowledgeEvidenceArtifactInventoryEntry
                                                ::artifactId
                                )
                        )
                        .map(
                                e ->
                                        String.join(
                                                "|",
                                                e.artifactId(),
                                                Long.toString(e.size()),
                                                String.valueOf(
                                                        e.fingerprint()
                                                ),
                                                String.valueOf(
                                                        e.merkleRoot()
                                                ),
                                                Boolean.toString(
                                                        e.revoked()
                                                )
                                        )
                        )
                        .toList();

        String canonical =
                String.join(
                        "\n",
                        entries
                );

        try {

            var digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return "sha256:" +
                    HexFormat.of().formatHex(
                            digest.digest(
                                    canonical.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint inventory",
                    e
            );
        }
    }
}
