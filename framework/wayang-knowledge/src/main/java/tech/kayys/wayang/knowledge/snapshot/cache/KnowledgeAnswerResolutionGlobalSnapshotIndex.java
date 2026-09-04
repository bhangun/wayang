package tech.kayys.wayang.knowledge.snapshot.cache;

import java.util.List;

public interface KnowledgeAnswerResolutionGlobalSnapshotIndex {

    void index(KnowledgeAnswerResolutionSnapshotBlockReference reference);

    List<KnowledgeAnswerResolutionSnapshotBlockReference> findBySnapshot(String snapshotId);

    List<KnowledgeAnswerResolutionSnapshotBlockReference> findByBlock(KnowledgeAnswerResolutionStateBlockId blockId);

    void removeSnapshot(String snapshotId);
}
