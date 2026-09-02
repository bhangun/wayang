package tech.kayys.wayang.knowledge.snapshot.pack;

import java.time.Instant;
import java.util.Map;

public record KnowledgeSnapshotLineageEntry(
        String lineageId,
        String sourceKnowledgeId,
        String targetKnowledgeId,
        String relationType,
        String createdBy,
        Instant createdAt,
        boolean requiredForReplay,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotLineageEntry {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
