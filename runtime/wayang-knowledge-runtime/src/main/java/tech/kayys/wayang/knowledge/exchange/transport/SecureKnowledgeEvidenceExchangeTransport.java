package tech.kayys.wayang.knowledge.exchange.transport;

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
import java.util.concurrent.CompletableFuture;

public final class SecureKnowledgeEvidenceExchangeTransport
        implements KnowledgeEvidenceExchangeTransport {

    private final KnowledgeEvidenceExchangeTransport delegate;

    private final KnowledgeEvidenceExchangeProtocolSessionStore
            sessionStore;

    private final KnowledgeEvidenceExchangeProtocolRequestGuard
            requestGuard;

    public SecureKnowledgeEvidenceExchangeTransport(
            KnowledgeEvidenceExchangeTransport delegate,
            KnowledgeEvidenceExchangeProtocolSessionStore sessionStore,
            KnowledgeEvidenceExchangeProtocolRequestGuard requestGuard
    ) {

        this.delegate =
                Objects.requireNonNull(delegate);

        this.sessionStore =
                Objects.requireNonNull(sessionStore);

        this.requestGuard =
                Objects.requireNonNull(requestGuard);
    }

    @Override
    public KnowledgeEvidenceExchangeTransportType type() {
        return delegate.type();
    }

    @Override
    public KnowledgeEvidenceExchangeTransportDescriptor
    descriptor() {
        return delegate.descriptor();
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceExchangeTransportConnection
            > connect(
                    KnowledgeEvidenceExchangeTransportAddress address
            ) {

        return delegate.connect(address)
                .thenApply(connection ->
                        new SecureConnection(connection)
                );
    }

    private final class SecureConnection
            implements KnowledgeEvidenceExchangeTransportConnection {

        private final KnowledgeEvidenceExchangeTransportConnection
                delegateConnection;

        private SecureConnection(
                KnowledgeEvidenceExchangeTransportConnection
                        delegateConnection
        ) {
            this.delegateConnection =
                    delegateConnection;
        }

        @Override
        public String connectionId() {
            return delegateConnection.connectionId();
        }

        @Override
        public KnowledgeEvidenceExchangeTransportDescriptor
        descriptor() {
            return delegateConnection.descriptor();
        }

        @Override
        public KnowledgeEvidenceExchangeTransportState state() {
            return delegateConnection.state();
        }

        @Override
        public CompletableFuture<
                KnowledgeEvidenceExchangeTransportResponse
                > send(
                        KnowledgeEvidenceExchangeTransportRequest request
                ) {

            Instant now = Instant.now();

            if (request.sessionId() == null) {

                return CompletableFuture.failedFuture(
                        new KnowledgeEvidenceExchangeTransportException(
                                "Secure transport requires sessionId"
                        )
                );
            }

            var session =
                    sessionStore.find(
                            request.sessionId()
                    ).orElse(null);

            if (session == null) {

                return CompletableFuture.failedFuture(
                        new KnowledgeEvidenceExchangeTransportException(
                                "Unknown secure session"
                        )
                );
            }

            requestGuard.requireEstablished(
                    session,
                    now
            );

            return delegateConnection.send(request);
        }

        @Override
        public java.util.concurrent.Flow.Publisher<
                KnowledgeEvidenceExchangeTransportMessage
                > incoming() {
            return delegateConnection.incoming();
        }

        @Override
        public void cancel(String requestId) {
            delegateConnection.cancel(requestId);
        }

        @Override
        public void close() {
            delegateConnection.close();
        }
    }
}
