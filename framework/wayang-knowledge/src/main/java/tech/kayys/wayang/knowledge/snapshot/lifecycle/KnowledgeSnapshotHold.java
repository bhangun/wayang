package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeSnapshotHold(
        String holdId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotRetentionClass retentionClass,
        String tenantId,
        String reason,
        Instant createdAt,
        Instant expiresAt,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotHold {
        Objects.requireNonNull(holdId, "holdId");
        Objects.requireNonNull(snapshotId, "snapshotId");
        Objects.requireNonNull(retentionClass, "retentionClass");
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean activeAt(Instant now) {
        return expiresAt == null || expiresAt.isAfter(now);
    }
}
