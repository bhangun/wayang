package tech.kayys.wayang.knowledge.exchange.journal;

import java.time.Instant;
import java.util.Objects;

public final class DefaultKnowledgeAnswerResolutionJournalAppender
        implements KnowledgeAnswerResolutionJournalAppender {

    private final KnowledgeAnswerResolutionJournal journal;

    public DefaultKnowledgeAnswerResolutionJournalAppender(KnowledgeAnswerResolutionJournal journal) {
        this.journal = Objects.requireNonNull(journal, "journal");
    }

    @Override
    public synchronized KnowledgeAnswerResolutionLogEntry append(
            long term,
            String epochId,
            String keyFingerprint,
            String consensusId,
            String runtimeId,
            KnowledgeAnswerResolutionLogEntryType type,
            String payloadFingerprint,
            Instant createdAt,
            String metadata) {

        long nextIndex = journal.lastIndex() + 1;

        KnowledgeAnswerResolutionLogEntry entry = new KnowledgeAnswerResolutionLogEntry(
                nextIndex,
                term,
                epochId,
                keyFingerprint,
                consensusId,
                runtimeId,
                type,
                payloadFingerprint,
                createdAt != null ? createdAt : Instant.now(),
                metadata
        );

        return journal.append(entry);
    }
}
