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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeRuntimeIdentityRegistry
        implements KnowledgeEvidenceExchangeRuntimeIdentityRegistry {

    private final ConcurrentMap<String,
            KnowledgeEvidenceExchangeRuntimeIdentity> identities =
            new ConcurrentHashMap<>();

    private String identityKey(
            String runtimeId,
            String identityVersion
    ) {
        return runtimeId + ":" + identityVersion;
    }

    @Override
    public void register(
            KnowledgeEvidenceExchangeRuntimeIdentity identity
    ) {

        String key = identityKey(
                identity.runtimeId(),
                identity.identityVersion()
        );

        identities.compute(key, (ignored, existing) -> {

            if (existing == null) {
                return identity;
            }

            if (!existing.equals(identity)) {
                throw new IllegalStateException(
                        "Runtime identity already exists: " + key
                );
            }

            return existing;
        });
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeRuntimeIdentity> find(
            String runtimeId,
            String identityVersion
    ) {

        return Optional.ofNullable(
                identities.get(
                        identityKey(runtimeId, identityVersion)
                )
        );
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeRuntimeIdentity> resolve(
            String runtimeId,
            java.time.Instant at
    ) {

        return identities.values()
                .stream()
                .filter(identity ->
                        runtimeId.equals(identity.runtimeId()))
                .filter(identity -> identity.activeAt(at))
                .findFirst();
    }

    @Override
    public List<KnowledgeEvidenceExchangeRuntimeIdentity> findByTenant(
            String tenantId
    ) {

        List<KnowledgeEvidenceExchangeRuntimeIdentity> result =
                new ArrayList<>();

        for (var identity : identities.values()) {
            if (tenantId != null &&
                    tenantId.equals(identity.tenantId())) {
                result.add(identity);
            }
        }

        return List.copyOf(result);
    }

    @Override
    public List<KnowledgeEvidenceExchangeRuntimeIdentity>
    findByOrganization(
            String organizationId
    ) {

        List<KnowledgeEvidenceExchangeRuntimeIdentity> result =
                new ArrayList<>();

        for (var identity : identities.values()) {
            if (organizationId != null &&
                    organizationId.equals(identity.organizationId())) {
                result.add(identity);
            }
        }

        return List.copyOf(result);
    }

    @Override
    public void suspend(
            String runtimeId,
            String reason
    ) {

        updateStatus(
                runtimeId,
                KnowledgeEvidenceExchangeRuntimeIdentityStatus.SUSPENDED,
                reason
        );
    }

    @Override
    public void revoke(
            String runtimeId,
            String reason
    ) {

        updateStatus(
                runtimeId,
                KnowledgeEvidenceExchangeRuntimeIdentityStatus.REVOKED,
                reason
        );
    }

    private void updateStatus(
            String runtimeId,
            KnowledgeEvidenceExchangeRuntimeIdentityStatus status,
            String reason
    ) {

        identities.replaceAll((key, identity) -> {

            if (!runtimeId.equals(identity.runtimeId())) {
                return identity;
            }

            var metadata =
                    new java.util.HashMap<>(identity.metadata());

            if (reason != null) {
                metadata.put(
                        "statusReason",
                        reason
                );
            }

            return new KnowledgeEvidenceExchangeRuntimeIdentity(
                    identity.runtimeId(),
                    identity.identityVersion(),
                    identity.displayName(),
                    identity.runtimeType(),
                    identity.organizationId(),
                    identity.tenantId(),
                    identity.createdAt(),
                    identity.validFrom(),
                    identity.validUntil(),
                    status,
                    identity.identityFingerprint(),
                    identity.primaryKeyId(),
                    identity.primaryKeyVersion(),
                    identity.trustAnchorId(),
                    metadata
            );
        });
    }
}
