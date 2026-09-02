package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class KnowledgeSnapshotEvidencePackageBuilder {

    private String packageId;
    private KnowledgeSnapshotVerificationManifest manifest;
    private KnowledgeSnapshotVerificationInstructions instructions;
    private final List<KnowledgeSnapshotPackageResource> resources = new ArrayList<>();
    private Map<String, String> metadata = Map.of();

    public KnowledgeSnapshotEvidencePackageBuilder packageId(String packageId) {
        this.packageId = packageId;
        return this;
    }

    public KnowledgeSnapshotEvidencePackageBuilder manifest(KnowledgeSnapshotVerificationManifest manifest) {
        this.manifest = manifest;
        return this;
    }

    public KnowledgeSnapshotEvidencePackageBuilder instructions(KnowledgeSnapshotVerificationInstructions instructions) {
        this.instructions = instructions;
        return this;
    }

    public KnowledgeSnapshotEvidencePackageBuilder resource(KnowledgeSnapshotPackageResource resource) {
        if (resource != null) {
            this.resources.add(resource);
        }
        return this;
    }

    public KnowledgeSnapshotEvidencePackageBuilder resources(List<KnowledgeSnapshotPackageResource> resources) {
        if (resources != null) {
            this.resources.addAll(resources);
        }
        return this;
    }

    public KnowledgeSnapshotEvidencePackageBuilder metadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        return this;
    }

    public KnowledgeSnapshotEvidencePackage build() {
        if (manifest == null) {
            throw new IllegalStateException("Verification manifest is required");
        }
        String id = packageId != null ? packageId : UUID.randomUUID().toString();
        return new KnowledgeSnapshotEvidencePackage(id, manifest, instructions, resources, metadata);
    }
}
