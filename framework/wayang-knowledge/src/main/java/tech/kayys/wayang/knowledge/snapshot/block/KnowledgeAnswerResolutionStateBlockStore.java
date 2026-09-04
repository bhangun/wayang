package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.Optional;

public interface KnowledgeAnswerResolutionStateBlockStore {

    KnowledgeAnswerResolutionStateBlock put(KnowledgeAnswerResolutionStateBlock block);

    Optional<KnowledgeAnswerResolutionStateBlock> get(String blockId);

    boolean contains(String blockId);

    void delete(String blockId);
}
