package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Map;

public record KnowledgeSnapshotReferenceEntry(
        String id,
        String versionId,
        String kind,
        String fingerprint,
        boolean requiredForReplay,
        boolean requiredForAudit,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotReferenceEntry {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
