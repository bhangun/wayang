package tech.kayys.wayang.knowledge.exchange.journal;

import java.util.List;
import java.util.Optional;

public final class DefaultKnowledgeAnswerResolutionJournalRecovery
        implements KnowledgeAnswerResolutionJournalRecovery {

    @Override
    public KnowledgeAnswerResolutionJournalState recover(
            KnowledgeAnswerResolutionJournal journal,
            long lastAppliedIndex) {

        long lastIndex = journal.lastIndex();
        long committedIndex = lastIndex; // In recovered log, all committed entries are journaled
        long currentTerm = 0;
        String currentEpochId = null;

        if (lastIndex >= 0) {
            Optional<KnowledgeAnswerResolutionLogEntry> lastEntry = journal.get(lastIndex);
            if (lastEntry.isPresent()) {
                currentTerm = lastEntry.get().term();
                currentEpochId = lastEntry.get().epochId();
            }
        }

        return new KnowledgeAnswerResolutionJournalState(
                lastIndex,
                committedIndex,
                Math.min(lastAppliedIndex, committedIndex),
                currentTerm,
                currentEpochId
        );
    }

    @Override
    public List<KnowledgeAnswerResolutionLogEntry> replayEntries(
            KnowledgeAnswerResolutionJournal journal,
            long fromIndex,
            long toIndex) {

        return journal.range(fromIndex, toIndex);
    }
}
