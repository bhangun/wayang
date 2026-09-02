package tech.kayys.wayang.knowledge.exchange.routing;

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
import tech.kayys.wayang.knowledge.exchange.transfer.*;
import tech.kayys.wayang.knowledge.exchange.replication.*;
import tech.kayys.wayang.knowledge.exchange.sync.*;
import tech.kayys.wayang.knowledge.exchange.federation.*;
import tech.kayys.wayang.knowledge.exchange.routing.*;
import tech.kayys.wayang.knowledge.exchange.fusion.*;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeEvidenceRuntimeRetrieverRegistry
        implements KnowledgeEvidenceRuntimeRetrieverRegistry {

    private final Map<
            String,
            KnowledgeEvidenceRuntimeRetriever
            > retrievers =
            new ConcurrentHashMap<>();

    @Override
    public KnowledgeEvidenceRuntimeRetriever get(
            String runtimeId
    ) {

        return retrievers.get(runtimeId);
    }

    @Override
    public void register(
            KnowledgeEvidenceRuntimeRetriever retriever
    ) {

        if (retriever == null) {
            throw new IllegalArgumentException(
                    "retriever is required"
            );
        }

        retrievers.put(
                retriever.runtimeId(),
                retriever
        );
    }

    @Override
    public void remove(
            String runtimeId
    ) {

        retrievers.remove(runtimeId);
    }
}
