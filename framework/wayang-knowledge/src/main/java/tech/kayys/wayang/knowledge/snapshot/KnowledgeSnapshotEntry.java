package tech.kayys.wayang.knowledge.snapshot;

import java.util.Map;

/**
 * Versioned knowledge material included in a replay snapshot.
 */
public record KnowledgeSnapshotEntry(
        String knowledgeId,
        String versionId,
        String fingerprint,
        String provenanceId,
        String authorityFingerprint,
        String trustFingerprint,
        String lineageFingerprint,
        Map<String, Object> metadata
) {

    public KnowledgeSnapshotEntry {
        if (knowledgeId == null || knowledgeId.isBlank()) {
            throw new IllegalArgumentException("knowledgeId is required");
        }
        versionId = versionId == null ? "" : versionId;
        fingerprint = fingerprint == null ? "" : fingerprint;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
