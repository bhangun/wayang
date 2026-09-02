package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.time.Instant;
import java.util.Map;

public record KnowledgeEvidenceArtifactMetadata(
        KnowledgeEvidenceArtifactId artifactId,
        String mediaType,
        long size,
        Instant createdAt,
        String producer,
        String schemaVersion,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceArtifactMetadata {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
