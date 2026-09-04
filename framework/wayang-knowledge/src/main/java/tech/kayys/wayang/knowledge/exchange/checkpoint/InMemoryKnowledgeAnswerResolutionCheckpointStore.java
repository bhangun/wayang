package tech.kayys.wayang.knowledge.exchange.checkpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public final class InMemoryKnowledgeAnswerResolutionCheckpointStore
        implements KnowledgeAnswerResolutionCheckpointStore {

    private final ConcurrentSkipListMap<Long, KnowledgeAnswerResolutionStateCheckpoint> checkpoints =
            new ConcurrentSkipListMap<>();

    @Override
    public synchronized void save(KnowledgeAnswerResolutionStateCheckpoint checkpoint) {
        checkpoints.put(checkpoint.lastAppliedIndex(), checkpoint);
    }

    @Override
    public Optional<KnowledgeAnswerResolutionStateCheckpoint> latest() {
        return Optional.ofNullable(
                checkpoints.isEmpty() ? null : checkpoints.lastEntry().getValue());
    }

    @Override
    public Optional<KnowledgeAnswerResolutionStateCheckpoint> get(long lastAppliedIndex) {
        return Optional.ofNullable(checkpoints.get(lastAppliedIndex));
    }

    @Override
    public List<KnowledgeAnswerResolutionStateCheckpoint> list() {
        return new ArrayList<>(checkpoints.values());
    }

    @Override
    public synchronized void delete(long lastAppliedIndex) {
        checkpoints.remove(lastAppliedIndex);
    }
}
