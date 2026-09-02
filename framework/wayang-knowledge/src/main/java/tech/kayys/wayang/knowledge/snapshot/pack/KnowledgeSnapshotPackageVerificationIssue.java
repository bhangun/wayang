package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Map;

public record KnowledgeSnapshotPackageVerificationIssue(
        String code,
        String message,
        String resourceId,
        boolean fatal,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotPackageVerificationIssue {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
