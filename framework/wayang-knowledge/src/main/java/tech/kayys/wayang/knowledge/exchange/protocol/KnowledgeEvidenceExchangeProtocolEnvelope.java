package tech.kayys.wayang.knowledge.exchange.protocol;

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


import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeEvidenceExchangeProtocolEnvelope(

        String messageId,

        KnowledgeEvidenceExchangeProtocolMessageType type,

        String protocolVersion,

        String senderRuntimeId,

        String receiverRuntimeId,

        String sessionId,

        String correlationId,

        String nonce,

        Instant issuedAt,

        Instant expiresAt,

        String payloadFingerprint,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeProtocolEnvelope {
        Objects.requireNonNull(messageId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(protocolVersion);
        Objects.requireNonNull(senderRuntimeId);

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant at) {

        if (issuedAt != null &&
                at.isBefore(issuedAt)) {
            return false;
        }

        return expiresAt == null ||
                at.isBefore(expiresAt);
    }
}
