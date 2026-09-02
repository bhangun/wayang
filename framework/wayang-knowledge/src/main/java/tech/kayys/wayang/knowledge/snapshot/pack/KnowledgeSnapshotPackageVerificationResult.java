package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.List;
import java.util.Map;

public record KnowledgeSnapshotPackageVerificationResult(
        String packageId,
        KnowledgeSnapshotPackageVerificationStatus status,
        String snapshotId,
        String snapshotFingerprint,
        List<KnowledgeSnapshotPackageVerificationIssue> issues,
        boolean integrityVerified,
        boolean sealVerified,
        boolean dependenciesVerified,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotPackageVerificationResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean verified() {
        return status == KnowledgeSnapshotPackageVerificationStatus.VERIFIED;
    }
}
