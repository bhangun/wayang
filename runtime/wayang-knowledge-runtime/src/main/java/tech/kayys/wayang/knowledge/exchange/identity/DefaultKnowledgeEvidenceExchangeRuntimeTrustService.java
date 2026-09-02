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
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeRuntimeTrustService
        implements KnowledgeEvidenceExchangeRuntimeTrustService {

    private final KnowledgeEvidenceExchangeRuntimeIdentityRegistry registry;
    private final String policyId;

    public DefaultKnowledgeEvidenceExchangeRuntimeTrustService(
            KnowledgeEvidenceExchangeRuntimeIdentityRegistry registry,
            String policyId
    ) {
        this.registry = Objects.requireNonNull(registry);
        this.policyId = Objects.requireNonNull(policyId);
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeTrustDecision verify(
            String runtimeId,
            String expectedTenantId,
            Instant at
    ) {

        var identity =
                registry.resolve(runtimeId, at);

        if (identity.isEmpty()) {

            return new KnowledgeEvidenceExchangeRuntimeTrustDecision.Denied(
                    policyId,
                    KnowledgeEvidenceExchangeRuntimeIdentityStatus.UNKNOWN,
                    "Runtime identity is unknown or inactive",
                    java.util.Map.of(
                            "runtimeId",
                            runtimeId
                    )
            );
        }

        var value = identity.get();

        if (expectedTenantId != null &&
                value.tenantId() != null &&
                !expectedTenantId.equals(value.tenantId())) {

            return new KnowledgeEvidenceExchangeRuntimeTrustDecision.Denied(
                    policyId,
                    KnowledgeEvidenceExchangeRuntimeIdentityStatus.UNTRUSTED,
                    "Runtime tenant mismatch",
                    java.util.Map.of(
                            "expectedTenantId",
                            expectedTenantId,
                            "actualTenantId",
                            value.tenantId()
                    )
            );
        }

        return new KnowledgeEvidenceExchangeRuntimeTrustDecision.Trusted(
                policyId,
                java.util.Map.of(
                        "runtimeId",
                        runtimeId,
                        "identityVersion",
                        value.identityVersion()
                )
        );
    }
}
