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


import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class InputStreamKnowledgeEvidenceExchangeChunkPublisher
        implements KnowledgeEvidenceExchangeChunkPublisher {

    private final KnowledgeEvidenceExchangeChunkSource source;

    private final KnowledgeEvidenceExchangeChunkPolicy policy;

    private final String artifactId;

    private final String resourceId;

    private final String streamId;

    private final AtomicBoolean cancelled =
            new AtomicBoolean(false);

    public InputStreamKnowledgeEvidenceExchangeChunkPublisher(
            KnowledgeEvidenceExchangeChunkSource source,
            KnowledgeEvidenceExchangeChunkPolicy policy,
            String artifactId,
            String resourceId,
            String streamId
    ) {

        this.source = source;
        this.policy = policy;
        this.artifactId = artifactId;
        this.resourceId = resourceId;
        this.streamId = streamId;
    }

    @Override
    public void subscribe(
            Flow.Subscriber<? super KnowledgeEvidenceExchangeChunk>
                    subscriber
    ) {

        subscriber.onSubscribe(
                new Subscription(
                        subscriber
                )
        );
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public boolean cancelled() {
        return cancelled.get();
    }

    private final class Subscription
            implements Flow.Subscription {

        private final Flow.Subscriber
                <? super KnowledgeEvidenceExchangeChunk>
                subscriber;

        private final AtomicLong demand =
                new AtomicLong();

        private final AtomicLong sequence =
                new AtomicLong();

        private long offset;

        private InputStream input;

        private boolean opened;

        private boolean finished;

        private Subscription(
                Flow.Subscriber
                        <? super KnowledgeEvidenceExchangeChunk>
                        subscriber
        ) {
            this.subscriber = subscriber;
        }

        @Override
        public synchronized void request(long n) {

            if (n <= 0) {

                subscriber.onError(
                        new IllegalArgumentException(
                                "Demand must be positive"
                        )
                );

                return;
            }

            if (finished ||
                    cancelled()) {
                return;
            }

            demand.updateAndGet(
                    current -> {

                        long next =
                                current + n;

                        if (next < 0) {
                            return Long.MAX_VALUE;
                        }

                        return next;
                    }
            );

            emit();
        }

        private void emit() {

            try {

                if (!opened) {

                    input = source.open();
                    opened = true;
                }

                while (
                        demand.get() > 0 &&
                        !finished &&
                        !cancelled()
                ) {

                    int chunkSize =
                            policy.preferredChunkBytes();

                    byte[] buffer =
                            new byte[chunkSize];

                    int read =
                            input.read(buffer);

                    if (read < 0) {

                        finished = true;

                        input.close();

                        subscriber.onComplete();

                        return;
                    }

                    byte[] data =
                            java.util.Arrays.copyOf(
                                    buffer,
                                    read
                            );

                    boolean first =
                            offset == 0;

                    long currentOffset =
                            offset;

                    offset += read;

                    boolean last =
                            source.length() >= 0 &&
                            offset >= source.length();

                    var chunk =
                            new KnowledgeEvidenceExchangeChunk(
                                    artifactId,
                                    resourceId,
                                    streamId,
                                    sequence.getAndIncrement(),
                                    currentOffset,
                                    source.length(),
                                    data,
                                    null,
                                    first,
                                    last,
                                    java.util.Map.of()
                            );

                    subscriber.onNext(chunk);

                    demand.decrementAndGet();

                    if (last) {

                        finished = true;

                        input.close();

                        subscriber.onComplete();

                        return;
                    }
                }

            } catch (IOException e) {

                finished = true;

                subscriber.onError(e);
            }
        }

        @Override
        public void cancel() {

            InputStreamKnowledgeEvidenceExchangeChunkPublisher
                    .this.cancel();
        }
    }
}
