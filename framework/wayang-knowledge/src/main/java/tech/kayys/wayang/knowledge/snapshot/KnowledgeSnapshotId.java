package tech.kayys.wayang.knowledge.snapshot;

import java.time.Instant;

/**
 * Immutable identity of a knowledge/governance snapshot.
 */
public record KnowledgeSnapshotId(
        String value,
        String algorithm,
        Instant createdAt
) {

    public KnowledgeSnapshotId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("snapshot id value is required");
        }
        algorithm = algorithm == null ? "SHA-256" : algorithm;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static KnowledgeSnapshotId of(String value) {
        return new KnowledgeSnapshotId(value, "SHA-256", Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KnowledgeSnapshotId that)) return false;
        return java.util.Objects.equals(value, that.value) &&
               java.util.Objects.equals(algorithm, that.algorithm);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(value, algorithm);
    }

    @Override
    public String toString() {
        return algorithm + ":" + value;
    }
}
