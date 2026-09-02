package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeSnapshotReference(
        String referenceId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotReferenceType type,
        String ownerId,
        String tenantId,
        Instant createdAt,
        Instant expiresAt,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotReference {
        Objects.requireNonNull(referenceId, "referenceId");
        Objects.requireNonNull(snapshotId, "snapshotId");
        Objects.requireNonNull(type, "type");
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public boolean isActive(Instant now) {
        return !isExpired(now);
    }
}
