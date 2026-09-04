package tech.kayys.wayang.knowledge.exchange.checkpoint;

import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionJournal;

import java.util.Optional;

public final class DefaultKnowledgeAnswerResolutionCompactor
        implements KnowledgeAnswerResolutionCompactor {

    @Override
    public synchronized long compact(
            KnowledgeAnswerResolutionJournal journal,
            KnowledgeAnswerResolutionCheckpointStore checkpointStore,
            KnowledgeAnswerResolutionCheckpointPolicy policy) {

        if (journal == null || checkpointStore == null || policy == null) {
            return -1;
        }

        Optional<KnowledgeAnswerResolutionStateCheckpoint> latestOpt = checkpointStore.latest();
        if (latestOpt.isEmpty()) {
            return -1;
        }

        KnowledgeAnswerResolutionStateCheckpoint latest = latestOpt.get();
        long lastApplied = latest.lastAppliedIndex();
        long journalLast = journal.lastIndex();

        if (journalLast - lastApplied < policy.minimumCompactionEntries()) {
            return -1; // Not enough entries to warrant compaction
        }

        journal.truncateFrom(0); // in-memory truncate or prune up to checkpoint
        return lastApplied;
    }
}
