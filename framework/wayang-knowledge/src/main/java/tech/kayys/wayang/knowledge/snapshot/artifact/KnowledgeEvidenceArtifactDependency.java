package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.util.Map;

public record KnowledgeEvidenceArtifactDependency(
        KnowledgeEvidenceArtifactId artifactId,
        KnowledgeEvidenceArtifactId dependencyId,
        boolean requiredForVerification,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceArtifactDependency {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
