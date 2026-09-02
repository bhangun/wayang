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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public interface KnowledgeEvidenceExchangeTransportConnection
        extends AutoCloseable {

    String connectionId();

    KnowledgeEvidenceExchangeTransportDescriptor descriptor();

    KnowledgeEvidenceExchangeTransportState state();

    CompletableFuture<
            KnowledgeEvidenceExchangeTransportResponse
            > send(
                    KnowledgeEvidenceExchangeTransportRequest request
            );

    Flow.Publisher<KnowledgeEvidenceExchangeTransportMessage>
    incoming();

    void cancel(String requestId);

    void close();

    default boolean activeAt(Instant now) {
        return state() ==
                KnowledgeEvidenceExchangeTransportState.CONNECTED;
    }
}
