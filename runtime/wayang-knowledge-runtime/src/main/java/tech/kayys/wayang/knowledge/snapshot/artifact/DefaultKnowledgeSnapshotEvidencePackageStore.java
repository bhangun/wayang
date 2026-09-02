package tech.kayys.wayang.knowledge.snapshot.artifact;

import tech.kayys.wayang.knowledge.snapshot.pack.KnowledgeSnapshotEvidencePackage;

import java.util.Optional;

public final class DefaultKnowledgeSnapshotEvidencePackageStore
        implements KnowledgeSnapshotEvidencePackageStore {

    private final KnowledgeEvidenceArtifactService artifactService;
    private final KnowledgeSnapshotEvidencePackageSerializer serializer;

    public DefaultKnowledgeSnapshotEvidencePackageStore(
            KnowledgeEvidenceArtifactService artifactService,
            KnowledgeSnapshotEvidencePackageSerializer serializer
    ) {
        this.artifactService = artifactService;
        this.serializer = serializer;
    }

    @Override
    public KnowledgeSnapshotEvidencePackageArtifact put(
            KnowledgeSnapshotEvidencePackage evidencePackage
    ) {
        byte[] serialized = serializer.serialize(evidencePackage);

        KnowledgeEvidenceArtifactPutResult result = artifactService.put(
                serialized,
                "application/vnd.wayang.evidence-package+json",
                "wayang",
                "1"
        );

        String snapshotId = evidencePackage.manifest() != null ? evidencePackage.manifest().snapshotId() : "unknown";

        return new KnowledgeSnapshotEvidencePackageArtifact(
                result.artifactId(),
                evidencePackage.packageId(),
                snapshotId,
                null,
                null
        );
    }

    @Override
    public Optional<KnowledgeSnapshotEvidencePackage> get(
            KnowledgeEvidenceArtifactId artifactId
    ) {
        Optional<KnowledgeEvidenceArtifact> artifact = artifactService.get(artifactId);
        if (artifact.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(serializer.deserialize(artifact.get().content()));
    }

    @Override
    public boolean exists(KnowledgeEvidenceArtifactId artifactId) {
        return artifactService.get(artifactId).isPresent();
    }
}
