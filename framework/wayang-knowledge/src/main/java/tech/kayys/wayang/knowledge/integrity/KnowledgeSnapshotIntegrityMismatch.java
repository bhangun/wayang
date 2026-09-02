package tech.kayys.wayang.knowledge.integrity;

import java.util.Map;

public record KnowledgeSnapshotIntegrityMismatch(
        KnowledgeSnapshotIntegrityMismatchType type,
        String subjectId,
        String expectedFingerprint,
        String actualFingerprint,
        String message,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotIntegrityMismatch {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
