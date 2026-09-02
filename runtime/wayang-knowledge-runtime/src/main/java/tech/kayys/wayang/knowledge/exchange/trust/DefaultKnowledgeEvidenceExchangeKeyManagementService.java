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
import java.util.UUID;

public final class DefaultKnowledgeEvidenceExchangeKeyManagementService
        implements KnowledgeEvidenceExchangeKeyManagementService {

    private final KnowledgeEvidenceExchangeKeyTrustRegistry registry;
    private final KnowledgeEvidenceExchangeKeyTrustService trustService;
    private final KnowledgeEvidenceExchangeKeyRotationService rotationService;
    private final KnowledgeEvidenceExchangeKeyLifecycleEventSink eventSink;

    public DefaultKnowledgeEvidenceExchangeKeyManagementService(
            KnowledgeEvidenceExchangeKeyTrustRegistry registry,
            KnowledgeEvidenceExchangeKeyTrustService trustService,
            KnowledgeEvidenceExchangeKeyRotationService rotationService,
            KnowledgeEvidenceExchangeKeyLifecycleEventSink eventSink
    ) {

        this.registry = Objects.requireNonNull(registry);
        this.trustService = Objects.requireNonNull(trustService);
        this.rotationService = Objects.requireNonNull(rotationService);
        this.eventSink = Objects.requireNonNull(eventSink);
    }

    @Override
    public void register(
            KnowledgeEvidenceExchangeTrustedKey key,
            String actorId
    ) {

        registry.register(key);

        eventSink.record(
                new KnowledgeEvidenceExchangeKeyLifecycleEvent(
                        UUID.randomUUID().toString(),
                        key.keyId(),
                        key.keyVersion(),
                        key.runtimeId(),
                        key.tenantId(),
                        KnowledgeEvidenceExchangeKeyLifecycleEvent.Type.REGISTERED,
                        actorId,
                        null,
                        Instant.now(),
                        java.util.Map.of()
                )
        );
    }

    @Override
    public KnowledgeEvidenceExchangeKeyRotation rotate(
            KnowledgeEvidenceExchangeTrustedKey previousKey,
            KnowledgeEvidenceExchangeTrustedKey newKey,
            Instant activatedAt,
            String actorId,
            String reason
    ) {

        var rotation = rotationService.rotate(
                previousKey,
                newKey,
                activatedAt,
                reason
        );

        eventSink.record(
                new KnowledgeEvidenceExchangeKeyLifecycleEvent(
                        UUID.randomUUID().toString(),
                        newKey.keyId(),
                        newKey.keyVersion(),
                        newKey.runtimeId(),
                        newKey.tenantId(),
                        KnowledgeEvidenceExchangeKeyLifecycleEvent.Type.ROTATED,
                        actorId,
                        reason,
                        Instant.now(),
                        java.util.Map.of(
                                "previousVersion",
                                previousKey.keyVersion()
                        )
                )
        );

        return rotation;
    }

    @Override
    public void revoke(
            String keyId,
            String keyVersion,
            String actorId,
            String reason
    ) {

        var existing = registry.find(
                keyId,
                keyVersion
        );

        registry.revoke(
                keyId,
                keyVersion,
                reason
        );

        existing.ifPresent(key ->
                eventSink.record(
                        new KnowledgeEvidenceExchangeKeyLifecycleEvent(
                                UUID.randomUUID().toString(),
                                keyId,
                                keyVersion,
                                key.runtimeId(),
                                key.tenantId(),
                                KnowledgeEvidenceExchangeKeyLifecycleEvent.Type.REVOKED,
                                actorId,
                                reason,
                                Instant.now(),
                                java.util.Map.of()
                        )
                )
        );
    }

    @Override
    public KnowledgeEvidenceExchangeKeyTrustDecision verify(
            String keyId,
            String keyVersion,
            String runtimeId,
            String tenantId,
            Instant at
    ) {

        return trustService.verify(
                keyId,
                keyVersion,
                runtimeId,
                tenantId,
                at
        );
    }
}
