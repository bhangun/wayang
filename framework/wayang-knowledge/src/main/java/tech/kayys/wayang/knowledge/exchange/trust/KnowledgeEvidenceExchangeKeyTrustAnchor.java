package tech.kayys.wayang.knowledge.exchange.trust;

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

public record KnowledgeEvidenceExchangeKeyTrustAnchor(

        String anchorId,

        KnowledgeSnapshotTrustAnchorType anchorType,

        String runtimeId,

        String tenantId,

        String authority,

        Instant createdAt,

        Instant expiresAt,

        boolean trusted,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeKeyTrustAnchor {
        Objects.requireNonNull(anchorId, "anchorId");
        Objects.requireNonNull(anchorType, "anchorType");
        Objects.requireNonNull(createdAt, "createdAt");

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant at) {

        if (!trusted) {
            return false;
        }

        if (expiresAt != null &&
                at.isAfter(expiresAt)) {
            return false;
        }

        return !at.isBefore(createdAt);
    }
}
