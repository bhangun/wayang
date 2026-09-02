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

public record KnowledgeEvidenceExchangeTrustedKey(

        String keyId,

        String keyVersion,

        KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm algorithm,

        String runtimeId,

        String tenantId,

        Instant validFrom,

        Instant validUntil,

        boolean trusted,

        boolean revoked,

        String trustAnchorId,

        Map<String, String> metadata

) {

    public KnowledgeEvidenceExchangeTrustedKey {
        Objects.requireNonNull(keyId, "keyId");
        Objects.requireNonNull(keyVersion, "keyVersion");
        Objects.requireNonNull(algorithm, "algorithm");

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public boolean isValidAt(Instant instant) {

        Objects.requireNonNull(instant, "instant");

        if (revoked) {
            return false;
        }

        if (!trusted) {
            return false;
        }

        if (validFrom != null && instant.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && instant.isAfter(validUntil)) {
            return false;
        }

        return true;
    }

    public KnowledgeEvidenceExchangeKeyTrustStatus trustStatus(
            Instant instant
    ) {

        if (revoked) {
            return KnowledgeEvidenceExchangeKeyTrustStatus.REVOKED;
        }

        if (!trusted) {
            return KnowledgeEvidenceExchangeKeyTrustStatus.UNTRUSTED;
        }

        if (validFrom != null && instant.isBefore(validFrom)) {
            return KnowledgeEvidenceExchangeKeyTrustStatus.NOT_YET_VALID;
        }

        if (validUntil != null && instant.isAfter(validUntil)) {
            return KnowledgeEvidenceExchangeKeyTrustStatus.EXPIRED;
        }

        return KnowledgeEvidenceExchangeKeyTrustStatus.TRUSTED;
    }
}
