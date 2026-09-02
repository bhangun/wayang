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
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeKeyTrustService
        implements KnowledgeEvidenceExchangeKeyTrustService {

    private final KnowledgeEvidenceExchangeKeyTrustRegistry registry;
    private final KnowledgeEvidenceExchangeKeyTrustPolicy policy;

    public DefaultKnowledgeEvidenceExchangeKeyTrustService(
            KnowledgeEvidenceExchangeKeyTrustRegistry registry,
            KnowledgeEvidenceExchangeKeyTrustPolicy policy
    ) {

        this.registry = Objects.requireNonNull(registry);
        this.policy = Objects.requireNonNull(policy);
    }

    @Override
    public KnowledgeEvidenceExchangeKeyTrustDecision verify(
            String keyId,
            String keyVersion,
            String expectedRuntimeId,
            String expectedTenantId,
            Instant at
    ) {

        var key = registry.find(
                keyId,
                keyVersion
        );

        if (key.isEmpty()) {

            return new KnowledgeEvidenceExchangeKeyTrustDecision.Denied(
                    "key-registry",
                    KnowledgeEvidenceExchangeKeyTrustStatus.UNKNOWN,
                    "Unknown key",
                    java.util.Map.of(
                            "keyId", keyId,
                            "keyVersion", keyVersion
                    )
            );
        }

        return policy.evaluate(
                key.get(),
                expectedRuntimeId,
                expectedTenantId,
                at
        );
    }
}
