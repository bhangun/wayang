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


import java.time.Instant;
import java.util.Map;

public record KnowledgeEvidenceExchangeRuntimeSecurityEvent(

        String eventId,

        String localRuntimeId,

        String remoteRuntimeId,

        Type type,

        boolean successful,

        String reason,

        Instant createdAt,

        Map<String, String> metadata

) {

    public enum Type {

        ENROLLMENT_REQUESTED,

        ENROLLMENT_APPROVED,

        ENROLLMENT_DENIED,

        HANDSHAKE_STARTED,

        HANDSHAKE_COMPLETED,

        HANDSHAKE_DENIED,

        PEER_TRUSTED,

        PEER_REVOKED
    }

    public KnowledgeEvidenceExchangeRuntimeSecurityEvent {
        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }
}
