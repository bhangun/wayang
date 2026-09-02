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

public final class DefaultKnowledgeEvidenceExchangeKeyTrustPolicy
        implements KnowledgeEvidenceExchangeKeyTrustPolicy {

    private final String policyId;

    public DefaultKnowledgeEvidenceExchangeKeyTrustPolicy(
            String policyId
    ) {
        this.policyId = Objects.requireNonNull(policyId);
    }

    @Override
    public KnowledgeEvidenceExchangeKeyTrustDecision evaluate(
            KnowledgeEvidenceExchangeTrustedKey key,
            String expectedRuntimeId,
            String expectedTenantId,
            Instant at
    ) {

        Objects.requireNonNull(key);
        Objects.requireNonNull(at);

        var status = key.trustStatus(at);

        if (status !=
                KnowledgeEvidenceExchangeKeyTrustStatus.TRUSTED) {

            return new KnowledgeEvidenceExchangeKeyTrustDecision.Denied(
                    policyId,
                    status,
                    "Key is not currently trusted",
                    java.util.Map.of(
                            "keyId", key.keyId(),
                            "keyVersion", key.keyVersion()
                    )
            );
        }

        if (expectedRuntimeId != null &&
                key.runtimeId() != null &&
                !expectedRuntimeId.equals(key.runtimeId())) {

            return new KnowledgeEvidenceExchangeKeyTrustDecision.Denied(
                    policyId,
                    KnowledgeEvidenceExchangeKeyTrustStatus.RUNTIME_MISMATCH,
                    "Signing runtime does not match expected runtime",
                    java.util.Map.of(
                            "expectedRuntimeId",
                            expectedRuntimeId,
                            "actualRuntimeId",
                            key.runtimeId()
                    )
            );
        }

        if (expectedTenantId != null &&
                key.tenantId() != null &&
                !expectedTenantId.equals(key.tenantId())) {

            return new KnowledgeEvidenceExchangeKeyTrustDecision.Denied(
                    policyId,
                    KnowledgeEvidenceExchangeKeyTrustStatus.TENANT_MISMATCH,
                    "Key tenant does not match expected tenant",
                    java.util.Map.of(
                            "expectedTenantId",
                            expectedTenantId,
                            "actualTenantId",
                            key.tenantId()
                    )
            );
        }

        return new KnowledgeEvidenceExchangeKeyTrustDecision.Trusted(
                policyId,
                java.util.Map.of(
                        "keyId", key.keyId(),
                        "keyVersion", key.keyVersion()
                )
        );
    }
}
