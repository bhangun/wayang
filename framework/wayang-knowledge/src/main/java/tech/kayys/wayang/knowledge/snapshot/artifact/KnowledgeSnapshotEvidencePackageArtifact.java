package tech.kayys.wayang.knowledge.snapshot.artifact;

public record KnowledgeSnapshotEvidencePackageArtifact(
        KnowledgeEvidenceArtifactId artifactId,
        String packageId,
        String snapshotId,
        KnowledgeEvidenceArtifactId manifestArtifactId,
        KnowledgeEvidenceArtifactId sealArtifactId
) {
}
