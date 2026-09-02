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


import java.util.List;
import java.util.Objects;

public final class CompositeKnowledgeEvidenceExchangeKeyLifecycleEventSink
        implements KnowledgeEvidenceExchangeKeyLifecycleEventSink {

    private final List<KnowledgeEvidenceExchangeKeyLifecycleEventSink>
            sinks;

    public CompositeKnowledgeEvidenceExchangeKeyLifecycleEventSink(
            List<KnowledgeEvidenceExchangeKeyLifecycleEventSink> sinks
    ) {

        this.sinks = sinks == null
                ? List.of()
                : sinks.stream()
                        .filter(Objects::nonNull)
                        .toList();
    }

    @Override
    public void record(
            KnowledgeEvidenceExchangeKeyLifecycleEvent event
    ) {

        for (var sink : sinks) {
            sink.record(event);
        }
    }
}
