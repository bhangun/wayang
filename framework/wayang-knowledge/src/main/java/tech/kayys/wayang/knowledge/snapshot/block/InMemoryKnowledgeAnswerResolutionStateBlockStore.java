package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeAnswerResolutionStateBlockStore
        implements KnowledgeAnswerResolutionStateBlockStore {

    private final Map<String, KnowledgeAnswerResolutionStateBlock> blocks =
            new ConcurrentHashMap<>();

    @Override
    public KnowledgeAnswerResolutionStateBlock put(KnowledgeAnswerResolutionStateBlock block) {
        blocks.putIfAbsent(block.blockId(), block);
        return blocks.get(block.blockId());
    }

    @Override
    public Optional<KnowledgeAnswerResolutionStateBlock> get(String blockId) {
        return Optional.ofNullable(blocks.get(blockId));
    }

    @Override
    public boolean contains(String blockId) {
        return blocks.containsKey(blockId);
    }

    @Override
    public void delete(String blockId) {
        blocks.remove(blockId);
    }
}
