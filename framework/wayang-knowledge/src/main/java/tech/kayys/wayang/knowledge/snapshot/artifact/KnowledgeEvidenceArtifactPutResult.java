package tech.kayys.wayang.knowledge.snapshot.artifact;

public record KnowledgeEvidenceArtifactPutResult(
        KnowledgeEvidenceArtifactId artifactId,
        boolean created,
        boolean deduplicated,
        long size
) {
}
