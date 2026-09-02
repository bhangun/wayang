package tech.kayys.wayang.knowledge.replay;

import java.time.Instant;
import java.util.Map;

public record KnowledgeReplayAuditEvent(
        String id,
        String traceId,
        String replayId,
        KnowledgeReplayMode mode,
        KnowledgeReplayStatus status,
        Instant createdAt,
        Map<String, Object> metadata
) {

    public KnowledgeReplayAuditEvent {
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
