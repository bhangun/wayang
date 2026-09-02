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


import java.util.EnumSet;
import java.util.Map;

public final class KnowledgeEvidenceExchangeFrameFactory {

    private final KnowledgeEvidenceExchangePayloadFingerprinter
            fingerprinter;

    public KnowledgeEvidenceExchangeFrameFactory(
            KnowledgeEvidenceExchangePayloadFingerprinter
                    fingerprinter
    ) {
        this.fingerprinter = fingerprinter;
    }

    public KnowledgeEvidenceExchangeFrame create(
            KnowledgeEvidenceExchangeFrameType type,
            String sessionId,
            String streamId,
            String requestId,
            long sequence,
            byte[] payload,
            boolean first,
            boolean last,
            Map<String, String> metadata
    ) {

        var flags =
                EnumSet.noneOf(
                        KnowledgeEvidenceExchangeFrameFlags.class
                );

        if (first) {
            flags.add(
                    KnowledgeEvidenceExchangeFrameFlags.FIRST
            );
        }

        if (last) {

            flags.add(
                    KnowledgeEvidenceExchangeFrameFlags.LAST
            );

            flags.add(
                    KnowledgeEvidenceExchangeFrameFlags.FIN
            );
        }

        flags.add(
                KnowledgeEvidenceExchangeFrameFlags.CHECKSUMMED
        );

        return new KnowledgeEvidenceExchangeFrame(
                (byte) 1,
                type,
                flags,
                sessionId,
                streamId,
                requestId,
                sequence,
                payload.length,
                payload,
                fingerprinter.fingerprint(payload),
                metadata
        );
    }
}
