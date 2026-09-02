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


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryKnowledgeEvidenceExchangeSequenceValidator
        implements KnowledgeEvidenceExchangeSequenceValidator {

    private final ConcurrentMap<String, Long> lastSeen =
            new ConcurrentHashMap<>();

    @Override
    public void accept(
            String streamId,
            long sequence
    ) {

        lastSeen.compute(
                streamId,
                (id, previous) -> {

                    if (previous != null &&
                            sequence <= previous) {

                        throw new KnowledgeEvidenceExchangeTransportException(
                                "Invalid stream sequence: "
                                        + sequence
                        );
                    }

                    return sequence;
                }
        );
    }
}
