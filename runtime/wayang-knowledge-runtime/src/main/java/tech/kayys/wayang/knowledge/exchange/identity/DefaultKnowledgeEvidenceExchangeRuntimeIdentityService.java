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

public final class DefaultKnowledgeEvidenceExchangeRuntimeIdentityService
        implements KnowledgeEvidenceExchangeRuntimeIdentityService {

    private final KnowledgeEvidenceExchangeRuntimeIdentityRegistry registry;
    private final KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter
            fingerprinter;

    public DefaultKnowledgeEvidenceExchangeRuntimeIdentityService(
            KnowledgeEvidenceExchangeRuntimeIdentityRegistry registry,
            KnowledgeEvidenceExchangeRuntimeIdentityFingerprinter
                    fingerprinter
    ) {

        this.registry = Objects.requireNonNull(registry);
        this.fingerprinter =
                Objects.requireNonNull(fingerprinter);
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeIdentity create(
            String runtimeId,
            String identityVersion,
            String displayName,
            String runtimeType,
            String organizationId,
            String tenantId,
            String primaryKeyId,
            String primaryKeyVersion,
            String trustAnchorId,
            Instant validFrom,
            Instant validUntil
    ) {

        var provisional =
                new KnowledgeEvidenceExchangeRuntimeIdentity(
                        runtimeId,
                        identityVersion,
                        displayName,
                        runtimeType,
                        organizationId,
                        tenantId,
                        Instant.now(),
                        validFrom,
                        validUntil,
                        KnowledgeEvidenceExchangeRuntimeIdentityStatus
                                .PENDING,
                        "",
                        primaryKeyId,
                        primaryKeyVersion,
                        trustAnchorId,
                        Map.of()
                );

        String fingerprint =
                fingerprinter.fingerprint(provisional);

        return new KnowledgeEvidenceExchangeRuntimeIdentity(
                runtimeId,
                identityVersion,
                displayName,
                runtimeType,
                organizationId,
                tenantId,
                provisional.createdAt(),
                validFrom,
                validUntil,
                provisional.status(),
                fingerprint,
                primaryKeyId,
                primaryKeyVersion,
                trustAnchorId,
                Map.of()
        );
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeIdentity register(
            KnowledgeEvidenceExchangeRuntimeIdentity identity
    ) {

        registry.register(identity);

        return identity;
    }
}
