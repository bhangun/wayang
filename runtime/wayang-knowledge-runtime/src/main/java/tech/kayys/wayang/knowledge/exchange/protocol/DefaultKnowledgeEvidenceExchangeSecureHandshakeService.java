package tech.kayys.wayang.knowledge.exchange.protocol;

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

public final class DefaultKnowledgeEvidenceExchangeSecureHandshakeService
        implements KnowledgeEvidenceExchangeSecureHandshakeService {

    private final KnowledgeEvidenceExchangeProtocolSessionStore
            sessionStore;

    private final KnowledgeEvidenceExchangeProtocolStateMachine
            stateMachine;

    public DefaultKnowledgeEvidenceExchangeSecureHandshakeService(
            KnowledgeEvidenceExchangeProtocolSessionStore sessionStore,
            KnowledgeEvidenceExchangeProtocolStateMachine stateMachine
    ) {

        this.sessionStore =
                Objects.requireNonNull(sessionStore);

        this.stateMachine =
                Objects.requireNonNull(stateMachine);
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolSession initiate(
            KnowledgeEvidenceExchangeRuntimeIdentity localIdentity,
            KnowledgeEvidenceExchangeProtocolHello localHello,
            Instant now
    ) {

        stateMachine.transition(
                KnowledgeEvidenceExchangeProtocolMessageType.HELLO
        );

        String sessionId =
                UUID.randomUUID().toString();

        var session =
                new KnowledgeEvidenceExchangeProtocolSession(
                        sessionId,
                        localIdentity.runtimeId(),
                        null,
                        KnowledgeEvidenceExchangeProtocolState
                                .HELLO_SENT,
                        localHello.protocolVersion(),
                        localHello.nonce(),
                        null,
                        null,
                        localIdentity.identityFingerprint(),
                        null,
                        null,
                        null,
                        null,
                        now,
                        now.plusSeconds(300),
                        Map.of()
                );

        sessionStore.save(session);

        return session;
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolSession receiveHello(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeProtocolHello remoteHello,
            Instant now
    ) {

        if (!remoteHello.activeAt(now)) {
            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Remote HELLO expired"
            );
        }

        stateMachine.transition(
                KnowledgeEvidenceExchangeProtocolMessageType.HELLO
        );

        var updated =
                new KnowledgeEvidenceExchangeProtocolSession(
                        session.sessionId(),
                        session.localRuntimeId(),
                        remoteHello.runtimeId(),
                        KnowledgeEvidenceExchangeProtocolState
                                .HELLO_RECEIVED,
                        session.negotiatedProtocolVersion(),
                        session.localNonce(),
                        remoteHello.nonce(),
                        session.handshakeId(),
                        session.localIdentityFingerprint(),
                        session.remoteIdentityFingerprint(),
                        session.localCapabilityFingerprint(),
                        session.remoteCapabilityFingerprint(),
                        session.sessionFingerprint(),
                        session.createdAt(),
                        session.expiresAt(),
                        session.metadata()
                );

        sessionStore.save(updated);

        return updated;
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolSession authenticate(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeProtocolAuthenticationMessage
                    authentication,
            Instant now
    ) {

        if (session.state() !=
                KnowledgeEvidenceExchangeProtocolState
                        .IDENTITY_RECEIVED) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Authentication received before identity phase"
            );
        }

        stateMachine.transition(
                KnowledgeEvidenceExchangeProtocolMessageType
                        .AUTHENTICATE
        );

        var updated =
                new KnowledgeEvidenceExchangeProtocolSession(
                        session.sessionId(),
                        session.localRuntimeId(),
                        session.remoteRuntimeId(),
                        KnowledgeEvidenceExchangeProtocolState
                                .AUTHENTICATED,
                        session.negotiatedProtocolVersion(),
                        session.localNonce(),
                        session.remoteNonce(),
                        session.handshakeId(),
                        session.localIdentityFingerprint(),
                        authentication.identityFingerprint(),
                        session.localCapabilityFingerprint(),
                        session.remoteCapabilityFingerprint(),
                        session.sessionFingerprint(),
                        session.createdAt(),
                        session.expiresAt(),
                        session.metadata()
                );

        sessionStore.save(updated);

        return updated;
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolSession negotiate(
            KnowledgeEvidenceExchangeProtocolSession session,
            KnowledgeEvidenceExchangeCapabilityNegotiationResult
                    negotiation,
            Instant now
    ) {

        if (!negotiation.successful()) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Capability negotiation failed"
            );
        }

        stateMachine.transition(
                KnowledgeEvidenceExchangeProtocolMessageType
                        .NEGOTIATE
        );

        var updated =
                new KnowledgeEvidenceExchangeProtocolSession(
                        session.sessionId(),
                        session.localRuntimeId(),
                        session.remoteRuntimeId(),
                        KnowledgeEvidenceExchangeProtocolState
                                .NEGOTIATED,
                        negotiation.protocolVersion(),
                        session.localNonce(),
                        session.remoteNonce(),
                        session.handshakeId(),
                        session.localIdentityFingerprint(),
                        session.remoteIdentityFingerprint(),
                        negotiation.localManifestFingerprint(),
                        negotiation.remoteManifestFingerprint(),
                        session.sessionFingerprint(),
                        session.createdAt(),
                        session.expiresAt(),
                        session.metadata()
                );

        sessionStore.save(updated);

        return updated;
    }

    @Override
    public KnowledgeEvidenceExchangeProtocolSession establish(
            KnowledgeEvidenceExchangeProtocolSession session,
            String handshakeId,
            Instant now
    ) {

        if (session.state() !=
                KnowledgeEvidenceExchangeProtocolState
                        .NEGOTIATED) {

            throw new KnowledgeEvidenceExchangeProtocolStateException(
                    "Cannot establish before negotiation"
            );
        }

        stateMachine.transition(
                KnowledgeEvidenceExchangeProtocolMessageType
                        .ESTABLISH
        );

        String sessionFingerprint =
                session.localRuntimeId()
                        + "|"
                        + session.remoteRuntimeId()
                        + "|"
                        + session.negotiatedProtocolVersion()
                        + "|"
                        + session.localNonce()
                        + "|"
                        + session.remoteNonce()
                        + "|"
                        + session.localCapabilityFingerprint()
                        + "|"
                        + session.remoteCapabilityFingerprint();

        var updated =
                new KnowledgeEvidenceExchangeProtocolSession(
                        session.sessionId(),
                        session.localRuntimeId(),
                        session.remoteRuntimeId(),
                        KnowledgeEvidenceExchangeProtocolState
                                .ESTABLISHED,
                        session.negotiatedProtocolVersion(),
                        session.localNonce(),
                        session.remoteNonce(),
                        handshakeId,
                        session.localIdentityFingerprint(),
                        session.remoteIdentityFingerprint(),
                        session.localCapabilityFingerprint(),
                        session.remoteCapabilityFingerprint(),
                        sessionFingerprint,
                        session.createdAt(),
                        session.expiresAt(),
                        session.metadata()
                );

        sessionStore.save(updated);

        return updated;
    }
}
