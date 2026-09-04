package tech.kayys.wayang.knowledge.exchange.transfer;

import java.time.Instant;

public record KnowledgeAnswerResolutionSnapshotTransferCheckpoint(
        String transferId,
        String snapshotId,
        long nextOffset,
        long bytesTransferred,
        String partialFingerprint,
        Instant updatedAt
) {}
