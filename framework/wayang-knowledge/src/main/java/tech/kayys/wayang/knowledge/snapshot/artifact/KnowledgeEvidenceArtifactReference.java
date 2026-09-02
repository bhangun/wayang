package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.util.Map;

public record KnowledgeEvidenceArtifactReference(
        KnowledgeEvidenceArtifactId artifactId,
        String role,
        boolean requiredForVerification,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceArtifactReference {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
