package tech.kayys.wayang.knowledge.exchange.journal;

import java.util.List;

public interface KnowledgeAnswerResolutionJournalRecovery {

    KnowledgeAnswerResolutionJournalState recover(
            KnowledgeAnswerResolutionJournal journal,
            long lastAppliedIndex
    );

    List<KnowledgeAnswerResolutionLogEntry> replayEntries(
            KnowledgeAnswerResolutionJournal journal,
            long fromIndex,
            long toIndex
    );
}
