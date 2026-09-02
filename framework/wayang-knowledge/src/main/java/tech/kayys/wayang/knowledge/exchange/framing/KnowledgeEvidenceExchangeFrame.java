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


import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record KnowledgeEvidenceExchangeFrame(

        byte version,

        KnowledgeEvidenceExchangeFrameType type,

        Set<KnowledgeEvidenceExchangeFrameFlags> flags,

        String sessionId,

        String streamId,

        String requestId,

        long sequence,

        long payloadLength,

        byte[] payload,

        String payloadFingerprint,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeFrame {

        Objects.requireNonNull(type);

        flags = flags == null
                ? EnumSet.noneOf(
                        KnowledgeEvidenceExchangeFrameFlags.class)
                : EnumSet.copyOf(flags);

        payload = payload == null
                ? new byte[0]
                : payload.clone();

        if (sequence < 0) {
            throw new IllegalArgumentException(
                    "sequence must be >= 0"
            );
        }

        if (payloadLength < 0) {
            throw new IllegalArgumentException(
                    "payloadLength must be >= 0"
            );
        }

        if (payloadLength != payload.length) {
            throw new IllegalArgumentException(
                    "payloadLength does not match payload"
            );
        }

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    public boolean first() {
        return flags.contains(
                KnowledgeEvidenceExchangeFrameFlags.FIRST
        );
    }

    public boolean last() {
        return flags.contains(
                KnowledgeEvidenceExchangeFrameFlags.LAST
        );
    }

    public boolean finalFrame() {
        return flags.contains(
                KnowledgeEvidenceExchangeFrameFlags.FIN
        );
    }
}
