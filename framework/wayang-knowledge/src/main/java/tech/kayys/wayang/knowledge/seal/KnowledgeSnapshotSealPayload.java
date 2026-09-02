package tech.kayys.wayang.knowledge.seal;

import java.time.Instant;

public record KnowledgeSnapshotSealPayload(
        String snapshotId,
        String snapshotFingerprint,
        String integrityStatus,
        String verifierId,
        String verifierVersion,
        Instant verifiedAt
) {}
