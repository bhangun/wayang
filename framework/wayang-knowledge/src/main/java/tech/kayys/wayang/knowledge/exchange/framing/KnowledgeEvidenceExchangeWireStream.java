package tech.kayys.wayang.knowledge.exchange.framing;

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


public final class KnowledgeEvidenceExchangeWireStream {

    private final String streamId;

    private final String sessionId;

    private final KnowledgeEvidenceExchangeStreamSequence
            sequence =
            new KnowledgeEvidenceExchangeStreamSequence();

    private final KnowledgeEvidenceExchangeFlowControlWindow
            sendWindow;

    private final KnowledgeEvidenceExchangeFlowControlWindow
            receiveWindow;

    public KnowledgeEvidenceExchangeWireStream(
            String streamId,
            String sessionId,
            long initialWindowBytes
    ) {

        this.streamId = streamId;
        this.sessionId = sessionId;

        this.sendWindow =
                new KnowledgeEvidenceExchangeFlowControlWindow(
                        initialWindowBytes
                );

        this.receiveWindow =
                new KnowledgeEvidenceExchangeFlowControlWindow(
                        initialWindowBytes
                );
    }

    public String streamId() {
        return streamId;
    }

    public String sessionId() {
        return sessionId;
    }

    public long nextSequence() {
        return sequence.next();
    }

    public KnowledgeEvidenceExchangeFlowControlWindow
    sendWindow() {
        return sendWindow;
    }

    public KnowledgeEvidenceExchangeFlowControlWindow
    receiveWindow() {
        return receiveWindow;
    }
}
