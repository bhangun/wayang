package tech.kayys.wayang.knowledge.exchange.transfer;

import java.time.Instant;

public record KnowledgeAnswerResolutionSnapshotDescriptor(
        String snapshotId,
        String runtimeId,
        String tenantId,
        String epochId,
        long lastAppliedIndex,
        long term,
        long totalBytes,
        int chunkSize,
        String stateFingerprint,
        String merkleRoot,
        Instant createdAt,
        Instant expiresAt
) {}
