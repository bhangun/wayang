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


import java.util.concurrent.atomic.AtomicLong;

public final class KnowledgeEvidenceExchangeFlowControlWindow {

    private final AtomicLong availableBytes;

    public KnowledgeEvidenceExchangeFlowControlWindow(
            long initialBytes
    ) {

        if (initialBytes < 0) {
            throw new IllegalArgumentException(
                    "initialBytes must be >= 0"
            );
        }

        availableBytes =
                new AtomicLong(initialBytes);
    }

    public long available() {
        return availableBytes.get();
    }

    public boolean tryConsume(long bytes) {

        if (bytes < 0) {
            throw new IllegalArgumentException(
                    "bytes must be >= 0"
            );
        }

        while (true) {

            long current =
                    availableBytes.get();

            if (current < bytes) {
                return false;
            }

            if (availableBytes.compareAndSet(
                    current,
                    current - bytes
            )) {

                return true;
            }
        }
    }

    public void update(long bytes) {

        if (bytes <= 0) {
            throw new IllegalArgumentException(
                    "bytes must be positive"
            );
        }

        availableBytes.addAndGet(bytes);
    }
}
