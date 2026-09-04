package tech.kayys.wayang.knowledge.exchange.journal;

import java.time.Instant;
import java.util.Objects;

public record KnowledgeAnswerResolutionLogEntry(
        long index,
        long term,
        String epochId,
        String keyFingerprint,
        String consensusId,
        String runtimeId,
        KnowledgeAnswerResolutionLogEntryType type,
        String payloadFingerprint,
        Instant createdAt,
        String metadata
) {
    public KnowledgeAnswerResolutionLogEntry {
        Objects.requireNonNull(epochId, "epochId");
        Objects.requireNonNull(keyFingerprint, "keyFingerprint");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payloadFingerprint, "payloadFingerprint");
        Objects.requireNonNull(createdAt, "createdAt");

        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        if (term < 0) {
            throw new IllegalArgumentException("term must be >= 0");
        }
    }
}
