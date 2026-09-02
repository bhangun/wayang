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


import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeFrameIntegrityVerifier
        implements KnowledgeEvidenceExchangeFrameIntegrityVerifier {

    private final KnowledgeEvidenceExchangePayloadFingerprinter
            fingerprinter;

    public DefaultKnowledgeEvidenceExchangeFrameIntegrityVerifier(
            KnowledgeEvidenceExchangePayloadFingerprinter fingerprinter
    ) {
        this.fingerprinter =
                Objects.requireNonNull(fingerprinter);
    }

    @Override
    public void verify(
            KnowledgeEvidenceExchangeFrame frame
    ) {

        if (frame.payloadFingerprint() == null) {
            return;
        }

        String actual =
                fingerprinter.fingerprint(
                        frame.payload()
                );

        if (!actual.equals(
                frame.payloadFingerprint()
        )) {

            throw new KnowledgeEvidenceExchangeTransportException(
                    "Frame payload integrity failure"
            );
        }
    }
}
