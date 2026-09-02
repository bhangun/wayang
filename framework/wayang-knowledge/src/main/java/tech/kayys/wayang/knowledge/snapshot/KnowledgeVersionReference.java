package tech.kayys.wayang.knowledge.snapshot;

import java.util.Map;

/**
 * Immutable reference to a versioned runtime object.
 */
public record KnowledgeVersionReference(
        String id,
        String versionId,
        String kind,
        String fingerprint,
        Map<String, Object> metadata
) {

    public KnowledgeVersionReference {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        versionId = versionId == null ? "" : versionId;
        kind = kind == null ? "unknown" : kind;
        fingerprint = fingerprint == null ? "" : fingerprint;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
