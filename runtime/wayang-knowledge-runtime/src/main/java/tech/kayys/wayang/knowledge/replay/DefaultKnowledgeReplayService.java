package tech.kayys.wayang.knowledge.replay;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeReplayService implements KnowledgeReplayService {

    private final KnowledgeReplayEngine engine;
    private final KnowledgeReplayAuditSink auditSink;

    public DefaultKnowledgeReplayService(
            KnowledgeReplayEngine engine,
            KnowledgeReplayAuditSink auditSink) {

        this.engine = Objects.requireNonNull(engine, "engine is required");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink is required");
    }

    @Override
    public KnowledgeReplayResult replay(KnowledgeReplayRequest request) {
        KnowledgeReplayResult result = engine.replay(request);

        auditSink.publish(
                new KnowledgeReplayAuditEvent(
                        UUID.randomUUID().toString(),
                        result.traceId(),
                        result.replayId(),
                        request.mode(),
                        result.status(),
                        Instant.now(),
                        result.diagnostics()
                )
        );

        return result;
    }
}
