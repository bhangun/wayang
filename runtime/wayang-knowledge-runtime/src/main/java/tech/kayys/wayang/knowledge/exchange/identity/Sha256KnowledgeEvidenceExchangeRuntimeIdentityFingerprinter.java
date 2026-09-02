package tech.kayys.wayang.knowledge.exchange.identity;

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


import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;

public final class Sha256KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter
        implements KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter {

    private final KnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer
            canonicalizer;

    public Sha256KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter(
            KnowledgeEvidenceExchangeRuntimeIdentityCanonicalizer
                    canonicalizer
    ) {
        this.canonicalizer =
                Objects.requireNonNull(canonicalizer);
    }

    @Override
    public String fingerprint(
            KnowledgeEvidenceExchangeRuntimeIdentity identity
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                            canonicalizer.canonicalize(identity)
                    )
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to fingerprint runtime identity",
                    e
            );
        }
    }
}
