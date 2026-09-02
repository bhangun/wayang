package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.List;
import java.util.Map;

public record KnowledgeSnapshotEvidencePackage(
        String packageId,
        KnowledgeSnapshotVerificationManifest manifest,
        KnowledgeSnapshotVerificationInstructions instructions,
        List<KnowledgeSnapshotPackageResource> resources,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotEvidencePackage {
        resources = resources == null ? List.of() : List.copyOf(resources);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public KnowledgeSnapshotEvidencePackage(
            String packageId,
            KnowledgeSnapshotVerificationManifest manifest,
            List<KnowledgeSnapshotPackageResource> resources,
            Map<String, String> metadata
    ) {
        this(packageId, manifest, null, resources, metadata);
    }
}
