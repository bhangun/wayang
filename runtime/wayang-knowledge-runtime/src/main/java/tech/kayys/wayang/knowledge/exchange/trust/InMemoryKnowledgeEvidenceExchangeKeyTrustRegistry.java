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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeKeyTrustRegistry
        implements KnowledgeEvidenceExchangeKeyTrustRegistry {

    private final ConcurrentMap<String, KnowledgeEvidenceExchangeTrustedKey> keys =
            new ConcurrentHashMap<>();

    private String identity(
            String keyId,
            String keyVersion
    ) {
        return keyId + ":" + keyVersion;
    }

    @Override
    public void register(
            KnowledgeEvidenceExchangeTrustedKey key
    ) {

        String identity = identity(
                key.keyId(),
                key.keyVersion()
        );

        keys.compute(identity, (ignored, existing) -> {

            if (existing == null) {
                return key;
            }

            /*
             * Immutable identity:
             *
             * The same keyId/version must never silently
             * change into another key definition.
             */
            if (!existing.equals(key)) {
                throw new IllegalStateException(
                        "Key identity already exists with different definition: "
                                + identity
                );
            }

            return existing;
        });
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeTrustedKey> find(
            String keyId,
            String keyVersion
    ) {

        return Optional.ofNullable(
                keys.get(identity(keyId, keyVersion))
        );
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeTrustedKey> resolve(
            String keyId,
            String keyVersion,
            Instant at
    ) {

        return find(keyId, keyVersion)
                .filter(key -> key.isValidAt(at));
    }

    @Override
    public List<KnowledgeEvidenceExchangeTrustedKey> findByRuntime(
            String runtimeId
    ) {

        List<KnowledgeEvidenceExchangeTrustedKey> result =
                new ArrayList<>();

        for (KnowledgeEvidenceExchangeTrustedKey key : keys.values()) {
            if (runtimeId != null &&
                    runtimeId.equals(key.runtimeId())) {
                result.add(key);
            }
        }

        return List.copyOf(result);
    }

    @Override
    public List<KnowledgeEvidenceExchangeTrustedKey> findByTenant(
            String tenantId
    ) {

        List<KnowledgeEvidenceExchangeTrustedKey> result =
                new ArrayList<>();

        for (KnowledgeEvidenceExchangeTrustedKey key : keys.values()) {
            if (tenantId != null &&
                    tenantId.equals(key.tenantId())) {
                result.add(key);
            }
        }

        return List.copyOf(result);
    }

    @Override
    public void revoke(
            String keyId,
            String keyVersion,
            String reason
    ) {

        String identity = identity(keyId, keyVersion);

        keys.computeIfPresent(
                identity,
                (ignored, existing) -> {

                    var metadata = new java.util.HashMap<>(
                            existing.metadata()
                    );

                    if (reason != null) {
                        metadata.put(
                                "revocationReason",
                                reason
                        );
                    }

                    return new KnowledgeEvidenceExchangeTrustedKey(
                            existing.keyId(),
                            existing.keyVersion(),
                            existing.algorithm(),
                            existing.runtimeId(),
                            existing.tenantId(),
                            existing.validFrom(),
                            existing.validUntil(),
                            false,
                            true,
                            existing.trustAnchorId(),
                            metadata
                    );
                }
        );
    }

    @Override
    public boolean isTrusted(
            String keyId,
            String keyVersion,
            Instant at
    ) {

        return resolve(keyId, keyVersion, at).isPresent();
    }
}
