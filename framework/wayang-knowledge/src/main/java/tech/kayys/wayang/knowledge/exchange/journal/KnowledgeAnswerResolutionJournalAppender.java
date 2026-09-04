package tech.kayys.wayang.knowledge.exchange.journal;

import java.time.Instant;

public interface KnowledgeAnswerResolutionJournalAppender {

    KnowledgeAnswerResolutionLogEntry append(
            long term,
            String epochId,
            String keyFingerprint,
            String consensusId,
            String runtimeId,
            KnowledgeAnswerResolutionLogEntryType type,
            String payloadFingerprint,
            Instant createdAt,
            String metadata
    );
}
