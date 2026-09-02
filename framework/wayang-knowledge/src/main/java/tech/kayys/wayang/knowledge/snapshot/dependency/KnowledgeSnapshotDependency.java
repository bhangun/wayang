package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeSnapshotDependency(
        String dependencyId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotDependencyType type,
        String targetId,
        String targetVersionId,
        boolean requiredForReplay,
        boolean requiredForAudit,
        Instant createdAt,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotDependency {
        Objects.requireNonNull(dependencyId, "dependencyId");
        Objects.requireNonNull(snapshotId, "snapshotId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(targetId, "targetId");
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
