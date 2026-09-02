package tech.kayys.wayang.knowledge.snapshot.artifact;

import tech.kayys.wayang.knowledge.snapshot.pack.KnowledgeSnapshotEvidencePackage;

import java.util.Optional;

public interface KnowledgeSnapshotEvidencePackageStore {

    KnowledgeSnapshotEvidencePackageArtifact put(
            KnowledgeSnapshotEvidencePackage evidencePackage
    );

    Optional<KnowledgeSnapshotEvidencePackage> get(
            KnowledgeEvidenceArtifactId artifactId
    );

    boolean exists(
            KnowledgeEvidenceArtifactId artifactId
    );
}
