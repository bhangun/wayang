package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeSnapshotIntegrityResult(
        String verificationId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotIntegrityStatus status,
        Instant verifiedAt,
        List<KnowledgeSnapshotIntegrityMismatch> mismatches,
        List<String> verifiedDependencies,
        List<String> missingDependencies,
        String computedFingerprint,
        String expectedFingerprint,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotIntegrityResult {
        mismatches = mismatches == null ? List.of() : List.copyOf(mismatches);
        verifiedDependencies = verifiedDependencies == null ? List.of() : List.copyOf(verifiedDependencies);
        missingDependencies = missingDependencies == null ? List.of() : List.copyOf(missingDependencies);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isValid() {
        return status == KnowledgeSnapshotIntegrityStatus.ATTESTED;
    }
}
