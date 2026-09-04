package tech.kayys.wayang.knowledge.snapshot.cache;

import java.util.Optional;

public interface KnowledgeAnswerResolutionSharedStateBlockStore {

    KnowledgeAnswerResolutionSharedStateBlock put(KnowledgeAnswerResolutionSharedStateBlock block);

    Optional<KnowledgeAnswerResolutionSharedStateBlock> get(KnowledgeAnswerResolutionStateBlockId id);

    boolean contains(KnowledgeAnswerResolutionStateBlockId id);

    void delete(KnowledgeAnswerResolutionStateBlockId id);
}
