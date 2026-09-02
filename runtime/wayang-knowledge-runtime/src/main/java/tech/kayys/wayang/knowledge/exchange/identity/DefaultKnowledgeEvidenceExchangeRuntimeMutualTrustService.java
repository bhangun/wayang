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
import java.util.UUID;

public final class DefaultKnowledgeEvidenceExchangeRuntimeMutualTrustService
        implements KnowledgeEvidenceExchangeRuntimeMutualTrustService {

    private final KnowledgeEvidenceExchangeRuntimeTrustService trustService;
    private final String localTenantId;

    public DefaultKnowledgeEvidenceExchangeRuntimeMutualTrustService(
            KnowledgeEvidenceExchangeRuntimeTrustService trustService,
            String localTenantId
    ) {
        this.trustService =
                Objects.requireNonNull(trustService);

        this.localTenantId = localTenantId;
    }

    @Override
    public KnowledgeEvidenceExchangeRuntimeHandshakeResult establish(
            KnowledgeEvidenceExchangeRuntimeIdentity localIdentity,
            KnowledgeEvidenceExchangeRuntimeIdentity remoteIdentity,
            String localKeyId,
            String localKeyVersion,
            String remoteKeyId,
            String remoteKeyVersion,
            String localNonce,
            String remoteNonce,
            Instant now
    ) {

        Objects.requireNonNull(localIdentity);
        Objects.requireNonNull(remoteIdentity);
        Objects.requireNonNull(now);

        /*
         * Local runtime must be active too.
         */
        if (!localIdentity.activeAt(now)) {

            return denied(
                    localIdentity,
                    remoteIdentity,
                    localKeyId,
                    localKeyVersion,
                    remoteKeyId,
                    remoteKeyVersion,
                    localNonce,
                    remoteNonce,
                    now,
                    "Local runtime identity is not active"
            );
        }

        /*
         * Remote runtime must be independently trusted.
         */
        var remoteTrust =
                trustService.verify(
                        remoteIdentity.runtimeId(),
                        localTenantId,
                        now
                );

        if (remoteTrust
                instanceof KnowledgeEvidenceExchangeRuntimeTrustDecision.Denied denied) {

            return denied(
                    localIdentity,
                    remoteIdentity,
                    localKeyId,
                    localKeyVersion,
                    remoteKeyId,
                    remoteKeyVersion,
                    localNonce,
                    remoteNonce,
                    now,
                    denied.reason()
            );
        }

        var handshake =
                new KnowledgeEvidenceExchangeRuntimeHandshake(
                        UUID.randomUUID().toString(),
                        localIdentity.runtimeId(),
                        remoteIdentity.runtimeId(),
                        localIdentity.identityFingerprint(),
                        remoteIdentity.identityFingerprint(),
                        localKeyId,
                        localKeyVersion,
                        remoteKeyId,
                        remoteKeyVersion,
                        localNonce,
                        remoteNonce,
                        now,
                        now.plusSeconds(300),
                        true,
                        true,
                        true,
                        Map.of()
                );

        return new KnowledgeEvidenceExchangeRuntimeHandshakeResult(
                KnowledgeEvidenceExchangeRuntimeHandshakeStatus
                        .MUTUALLY_TRUSTED,
                handshake,
                null,
                Map.of()
        );
    }

    private KnowledgeEvidenceExchangeRuntimeHandshakeResult denied(
            KnowledgeEvidenceExchangeRuntimeIdentity localIdentity,
            KnowledgeEvidenceExchangeRuntimeIdentity remoteIdentity,
            String localKeyId,
            String localKeyVersion,
            String remoteKeyId,
            String remoteKeyVersion,
            String localNonce,
            String remoteNonce,
            Instant now,
            String reason
    ) {

        var handshake =
                new KnowledgeEvidenceExchangeRuntimeHandshake(
                        UUID.randomUUID().toString(),
                        localIdentity.runtimeId(),
                        remoteIdentity.runtimeId(),
                        localIdentity.identityFingerprint(),
                        remoteIdentity.identityFingerprint(),
                        localKeyId,
                        localKeyVersion,
                        remoteKeyId,
                        remoteKeyVersion,
                        localNonce,
                        remoteNonce,
                        now,
                        now.plusSeconds(300),
                        true,
                        false,
                        false,
                        Map.of()
                );

        return new KnowledgeEvidenceExchangeRuntimeHandshakeResult(
                KnowledgeEvidenceExchangeRuntimeHandshakeStatus.DENIED,
                handshake,
                reason,
                Map.of()
        );
    }
}
