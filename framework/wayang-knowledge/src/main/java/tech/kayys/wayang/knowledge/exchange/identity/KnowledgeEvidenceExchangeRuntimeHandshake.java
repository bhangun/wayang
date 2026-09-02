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
import java.util.Objects;

public record KnowledgeEvidenceExchangeRuntimeHandshake(

        String handshakeId,

        String localRuntimeId,

        String remoteRuntimeId,

        String localIdentityFingerprint,

        String remoteIdentityFingerprint,

        String localKeyId,

        String localKeyVersion,

        String remoteKeyId,

        String remoteKeyVersion,

        String localNonce,

        String remoteNonce,

        Instant issuedAt,

        Instant expiresAt,

        boolean localAuthenticated,

        boolean remoteAuthenticated,

        boolean mutuallyTrusted,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeRuntimeHandshake {
        Objects.requireNonNull(handshakeId);
        Objects.requireNonNull(localRuntimeId);
        Objects.requireNonNull(remoteRuntimeId);

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant at) {

        return mutuallyTrusted &&
                (expiresAt == null ||
                        at.isBefore(expiresAt));
    }
}
