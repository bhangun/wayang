package tech.kayys.wayang.knowledge.exchange.checkpoint;

import tech.kayys.wayang.knowledge.exchange.journal.KnowledgeAnswerResolutionJournal;

public interface KnowledgeAnswerResolutionCompactor {

    long compact(
            KnowledgeAnswerResolutionJournal journal,
            KnowledgeAnswerResolutionCheckpointStore checkpointStore,
            KnowledgeAnswerResolutionCheckpointPolicy policy
    );
}
