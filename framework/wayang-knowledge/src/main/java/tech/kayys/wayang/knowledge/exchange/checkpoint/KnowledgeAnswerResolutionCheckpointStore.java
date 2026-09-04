package tech.kayys.wayang.knowledge.exchange.checkpoint;

import java.util.List;
import java.util.Optional;

public interface KnowledgeAnswerResolutionCheckpointStore {

    void save(KnowledgeAnswerResolutionStateCheckpoint checkpoint);

    Optional<KnowledgeAnswerResolutionStateCheckpoint> latest();

    Optional<KnowledgeAnswerResolutionStateCheckpoint> get(long lastAppliedIndex);

    List<KnowledgeAnswerResolutionStateCheckpoint> list();

    void delete(long lastAppliedIndex);
}
