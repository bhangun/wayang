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
import java.util.List;
import java.util.Optional;

public interface KnowledgeEvidenceExchangeKeyTrustRegistry {

    void register(
            KnowledgeEvidenceExchangeTrustedKey key
    );

    Optional<KnowledgeEvidenceExchangeTrustedKey> find(
            String keyId,
            String keyVersion
    );

    Optional<KnowledgeEvidenceExchangeTrustedKey> resolve(
            String keyId,
            String keyVersion,
            Instant at
    );

    List<KnowledgeEvidenceExchangeTrustedKey> findByRuntime(
            String runtimeId
    );

    List<KnowledgeEvidenceExchangeTrustedKey> findByTenant(
            String tenantId
    );

    void revoke(
            String keyId,
            String keyVersion,
            String reason
    );

    boolean isTrusted(
            String keyId,
            String keyVersion,
            Instant at
    );
}
