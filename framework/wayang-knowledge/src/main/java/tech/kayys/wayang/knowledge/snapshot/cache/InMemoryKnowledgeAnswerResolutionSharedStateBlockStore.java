package tech.kayys.wayang.knowledge.snapshot.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAnswerResolutionSharedStateBlockStore
        implements KnowledgeAnswerResolutionSharedStateBlockStore {

    private final Map<KnowledgeAnswerResolutionStateBlockId, KnowledgeAnswerResolutionSharedStateBlock> blocks =
            new ConcurrentHashMap<>();

    @Override
    public KnowledgeAnswerResolutionSharedStateBlock put(KnowledgeAnswerResolutionSharedStateBlock block) {
        return blocks.computeIfAbsent(block.id(), ignored -> block);
    }

    @Override
    public Optional<KnowledgeAnswerResolutionSharedStateBlock> get(KnowledgeAnswerResolutionStateBlockId id) {
        return Optional.ofNullable(blocks.get(id));
    }

    @Override
    public boolean contains(KnowledgeAnswerResolutionStateBlockId id) {
        return blocks.containsKey(id);
    }

    @Override
    public void delete(KnowledgeAnswerResolutionStateBlockId id) {
        blocks.remove(id);
    }
}
