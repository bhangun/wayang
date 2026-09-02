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

public record KnowledgeEvidenceExchangeRuntimeIdentity(

        String runtimeId,

        String identityVersion,

        String displayName,

        String runtimeType,

        String organizationId,

        String tenantId,

        Instant createdAt,

        Instant validFrom,

        Instant validUntil,

        KnowledgeEvidenceExchangeRuntimeIdentityStatus status,

        String identityFingerprint,

        String primaryKeyId,

        String primaryKeyVersion,

        String trustAnchorId,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeRuntimeIdentity {
        Objects.requireNonNull(runtimeId, "runtimeId");
        Objects.requireNonNull(identityVersion, "identityVersion");
        Objects.requireNonNull(status, "status");

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant at) {

        if (status !=
                KnowledgeEvidenceExchangeRuntimeIdentityStatus.ACTIVE) {
            return false;
        }

        if (validFrom != null &&
                at.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null &&
                at.isAfter(validUntil)) {
            return false;
        }

        return true;
    }
}
