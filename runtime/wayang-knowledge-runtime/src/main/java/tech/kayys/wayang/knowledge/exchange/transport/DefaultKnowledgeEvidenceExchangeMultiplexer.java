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


import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultKnowledgeEvidenceExchangeMultiplexer
        implements KnowledgeEvidenceExchangeMultiplexer {

    private final KnowledgeEvidenceExchangeTransportConnection
            connection;

    private final ConcurrentMap<
            String,
            KnowledgeEvidenceExchangeStream
            > streams =
            new ConcurrentHashMap<>();

    public DefaultKnowledgeEvidenceExchangeMultiplexer(
            KnowledgeEvidenceExchangeTransportConnection connection
    ) {
        this.connection = connection;
    }

    @Override
    public KnowledgeEvidenceExchangeStream openStream(
            String sessionId
    ) {

        String streamId =
                UUID.randomUUID().toString();

        var stream =
                new Stream(
                        streamId,
                        sessionId
                );

        streams.put(
                streamId,
                stream
        );

        return stream;
    }

    @Override
    public CompletableFuture<
            KnowledgeEvidenceExchangeTransportResponse
            > send(
                    KnowledgeEvidenceExchangeStream stream,
                    KnowledgeEvidenceExchangeTransportRequest request
            ) {

        if (!stream.active()) {

            return CompletableFuture.failedFuture(
                    new KnowledgeEvidenceExchangeTransportException(
                            "Stream is closed"
                    )
            );
        }

        return connection.send(request);
    }

    @Override
    public void closeStream(String streamId) {

        var stream =
                streams.remove(streamId);

        if (stream != null) {
            stream.cancel();
        }
    }

    private final class Stream
            implements KnowledgeEvidenceExchangeStream {

        private final String streamId;
        private final String sessionId;

        private volatile boolean active = true;

        private Stream(
                String streamId,
                String sessionId
        ) {
            this.streamId = streamId;
            this.sessionId = sessionId;
        }

        @Override
        public String streamId() {
            return streamId;
        }

        @Override
        public String sessionId() {
            return sessionId;
        }

        @Override
        public CompletableFuture<
                KnowledgeEvidenceExchangeTransportResponse
                > send(
                        KnowledgeEvidenceExchangeTransportRequest request
                ) {

            return DefaultKnowledgeEvidenceExchangeMultiplexer.this
                    .send(
                            this,
                            request
                    );
        }

        @Override
        public void cancel() {

            active = false;

            connection.cancel(streamId);
        }

        @Override
        public boolean active() {
            return active;
        }
    }
}
