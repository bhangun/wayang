package tech.kayys.wayang.knowledge.exchange.transfer;

import java.time.Instant;

public record KnowledgeAnswerResolutionSnapshotTransferState(
        String transferId,
        String snapshotId,
        String sourceRuntimeId,
        String targetRuntimeId,
        KnowledgeAnswerResolutionSnapshotTransferStatus status,
        long totalBytes,
        long bytesTransferred,
        long nextOffset,
        String expectedFingerprint,
        String merkleRoot,
        Instant createdAt,
        Instant updatedAt
) {}
