package tech.kayys.wayang.knowledge.seal;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.List;

public record KnowledgeSnapshotSealVerificationResult(
        String verificationId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotSealStatus status,
        String keyId,
        String keyVersion,
        Instant verifiedAt,
        List<String> diagnostics
) {

    public KnowledgeSnapshotSealVerificationResult {
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }

    public boolean valid() {
        return status == KnowledgeSnapshotSealStatus.VERIFIED;
    }
}
