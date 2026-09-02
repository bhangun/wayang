package tech.kayys.wayang.knowledge.snapshot.artifact;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeEvidenceArtifactManifest(
        KnowledgeEvidenceArtifactId artifactId,
        String artifactType,
        String schemaVersion,
        long size,
        List<KnowledgeEvidenceArtifactReference> references,
        List<KnowledgeEvidenceArtifactDependency> dependencies,
        Instant createdAt,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceArtifactManifest {
        references = references == null ? List.of() : List.copyOf(references);
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
