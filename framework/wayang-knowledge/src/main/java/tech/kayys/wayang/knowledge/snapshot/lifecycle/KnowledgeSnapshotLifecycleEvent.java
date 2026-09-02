package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Map;

public record KnowledgeSnapshotLifecycleEvent(
        String eventId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotLifecycleState previousState,
        KnowledgeSnapshotLifecycleState newState,
        String reason,
        Instant createdAt,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotLifecycleEvent {
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
