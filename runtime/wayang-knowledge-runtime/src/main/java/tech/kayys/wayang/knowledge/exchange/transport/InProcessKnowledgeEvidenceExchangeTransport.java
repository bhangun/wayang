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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public final class InProcessKnowledgeEvidenceExchangeTransport
        implements KnowledgeEvidenceExchangeTransport {

    private final KnowledgeEvidenceExchangeProtocolEndpoint endpoint;

    public InProcessKnowledgeEvidenceExchangeTransport(
            KnowledgeEvidenceExchangeProtocolEndpoint endpoint
    ) {
        this.endpoint = endpoint;
    }

    @Override
    public KnowledgeEvidenceExchangeTransportType type() {
        return KnowledgeEvidenceExchangeTransportType.IN_PROCESS;
    }

    @Override
    public KnowledgeEvidenceExchangeTransportDescriptor descriptor() {

        return new KnowledgeEvidenceExchangeTransportDescriptor(
                "in-process",
                type(),
                java.util.Set.of(
                        KnowledgeEvidenceExchangeTransportCapability
                                .REQUEST_RESPONSE,
                        KnowledgeEvidenceExchangeTransportCapability
                                .BIDIRECTIONAL_STREAMING,
                        KnowledgeEvidenceExchangeTransportCapability
                                .CANCELLATION,
                        KnowledgeEvidenceExchangeTransportCapability
                                .LOCAL_ONLY
                ),
                "1.0",
                1024,
                64L * 1024L * 1024L,
                java.util.Map.of()
        );
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceExchangeTransportConnection
            > connect(
                    KnowledgeEvidenceExchangeTransportAddress address
            ) {

        return CompletableFuture.completedFuture(
                new Connection(endpoint)
        );
    }

    private static final class Connection
            implements KnowledgeEvidenceExchangeTransportConnection {

        private final String id =
                UUID.randomUUID().toString();

        private final KnowledgeEvidenceExchangeProtocolEndpoint
                endpoint;

        private final SubmissionPublisher<
                KnowledgeEvidenceExchangeTransportMessage
                > publisher =
                new SubmissionPublisher<>();

        private volatile KnowledgeEvidenceExchangeTransportState
                state =
                KnowledgeEvidenceExchangeTransportState.CONNECTED;

        private Connection(
                KnowledgeEvidenceExchangeProtocolEndpoint endpoint
        ) {
            this.endpoint = endpoint;
        }

        @Override
        public String connectionId() {
            return id;
        }

        @Override
        public KnowledgeEvidenceExchangeTransportDescriptor
        descriptor() {

            return new InProcessKnowledgeEvidenceExchangeTransport(
                    endpoint
            ).descriptor();
        }

        @Override
        public KnowledgeEvidenceExchangeTransportState state() {
            return state;
        }

        @Override
        public CompletableFuture<
                KnowledgeEvidenceExchangeTransportResponse
                > send(
                        KnowledgeEvidenceExchangeTransportRequest request
                ) {

            return CompletableFuture.supplyAsync(() -> {

                Instant now = Instant.now();

                try {

                    var response =
                            endpoint.handle(
                                    request.message()
                                            .protocolMessage()
                            );

                    if (response != null) {
                        publisher.submit(new KnowledgeEvidenceExchangeTransportMessage(
                                UUID.randomUUID().toString(),
                                response
                        ));
                    }

                    return new KnowledgeEvidenceExchangeTransportResponse(
                            request.requestId(),
                            true,
                            response == null
                                    ? null
                                    : new KnowledgeEvidenceExchangeTransportMessage(
                                            UUID.randomUUID().toString(),
                                            response
                                    ),
                            null,
                            null,
                            now,
                            Instant.now(),
                            java.util.Map.of()
                    );

                } catch (Exception e) {

                    return
                            KnowledgeEvidenceExchangeTransportResponse
                                    .failure(
                                            request.requestId(),
                                            "TRANSPORT_ERROR",
                                            e.getMessage(),
                                            now
                                    );
                }
            });
        }

        @Override
        public Flow.Publisher<
                KnowledgeEvidenceExchangeTransportMessage
                > incoming() {

            return publisher;
        }

        @Override
        public void cancel(String requestId) {
            // In-process implementation may interrupt
            // an associated future in a richer implementation.
        }

        @Override
        public void close() {

            state =
                    KnowledgeEvidenceExchangeTransportState.CLOSED;

            publisher.close();
        }
    }
}
