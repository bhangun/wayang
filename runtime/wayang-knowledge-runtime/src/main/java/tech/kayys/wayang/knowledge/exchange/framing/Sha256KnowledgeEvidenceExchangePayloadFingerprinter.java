package tech.kayys.wayang.knowledge.exchange.framing;

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
import java.security.NoSuchAlgorithmException;

public final class Sha256KnowledgeEvidenceExchangePayloadFingerprinter
        implements KnowledgeEvidenceExchangePayloadFingerprinter {

    @Override
    public String fingerprint(byte[] payload) {

        try {

            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(payload);

            StringBuilder result =
                    new StringBuilder(
                            "sha256:"
                    );

            for (byte b : digest) {

                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 unavailable",
                    e
            );
        }
    }
}
