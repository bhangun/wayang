package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Map;

public record KnowledgeSnapshotEvidenceEntry(
        String knowledgeId,
        String versionId,
        String fingerprint,
        String provenanceId,
        String authorityFingerprint,
        String trustFingerprint,
        String lineageFingerprint,
        boolean requiredForReplay,
        boolean requiredForAudit,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotEvidenceEntry {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
