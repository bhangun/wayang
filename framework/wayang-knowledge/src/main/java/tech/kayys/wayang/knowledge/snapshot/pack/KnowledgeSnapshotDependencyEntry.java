package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Map;

public record KnowledgeSnapshotDependencyEntry(
        String dependencyId,
        String type,
        String targetId,
        String targetVersionId,
        boolean requiredForReplay,
        boolean requiredForAudit,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotDependencyEntry {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
