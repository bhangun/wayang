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


import java.util.Map;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeRuntimeEnrollmentPolicy
        implements KnowledgeEvidenceExchangeRuntimeEnrollmentPolicy {

    private final String policyId;

    public DefaultKnowledgeEvidenceExchangeRuntimeEnrollmentPolicy(
            String policyId
    ) {
        this.policyId = Objects.requireNonNull(policyId);
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeEnrollmentDecision evaluate(
            KnowledgeEvidenceExchangeRuntimeEnrollmentRequest request
    ) {

        var identity = request.identity();

        if (identity.runtimeId().isBlank()) {

            return new KnowledgeEvidenceExchangeRuntimeEnrollmentDecision.Denied(
                    policyId,
                    "Runtime ID is required",
                    Map.of()
            );
        }

        if (identity.identityFingerprint() == null ||
                identity.identityFingerprint().isBlank()) {

            return new KnowledgeEvidenceExchangeRuntimeEnrollmentDecision.Denied(
                    policyId,
                    "Runtime identity fingerprint is required",
                    Map.of()
            );
        }

        if (identity.primaryKeyId() == null ||
                identity.primaryKeyId().isBlank()) {

            return new KnowledgeEvidenceExchangeRuntimeEnrollmentDecision.Denied(
                    policyId,
                    "Primary runtime key is required",
                    Map.of()
            );
        }

        /*
         * Default policy does not automatically trust
         * arbitrary remote runtimes.
         *
         * An extension can approve enrollment using:
         * - enterprise PKI
         * - KMS/HSM
         * - admin approval
         * - transparency log
         * - organization registry
         */
        return new KnowledgeEvidenceExchangeRuntimeEnrollmentDecision.Pending(
                policyId,
                "Runtime requires explicit trust approval",
                Map.of(
                        "runtimeId",
                        identity.runtimeId()
                )
        );
    }
}
