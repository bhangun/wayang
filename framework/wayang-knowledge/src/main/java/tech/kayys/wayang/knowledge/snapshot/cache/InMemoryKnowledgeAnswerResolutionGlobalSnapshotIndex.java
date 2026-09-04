package tech.kayys.wayang.knowledge.snapshot.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryKnowledgeAnswerResolutionGlobalSnapshotIndex
        implements KnowledgeAnswerResolutionGlobalSnapshotIndex {

    private final Map<String, List<KnowledgeAnswerResolutionSnapshotBlockReference>> snapshotIndex =
            new ConcurrentHashMap<>();
    private final Map<KnowledgeAnswerResolutionStateBlockId, List<KnowledgeAnswerResolutionSnapshotBlockReference>> blockIndex =
            new ConcurrentHashMap<>();

    @Override
    public void index(KnowledgeAnswerResolutionSnapshotBlockReference reference) {
        snapshotIndex.computeIfAbsent(reference.snapshotId(), k -> new CopyOnWriteArrayList<>()).add(reference);
        blockIndex.computeIfAbsent(reference.blockId(), k -> new CopyOnWriteArrayList<>()).add(reference);
    }

    @Override
    public List<KnowledgeAnswerResolutionSnapshotBlockReference> findBySnapshot(String snapshotId) {
        return List.copyOf(snapshotIndex.getOrDefault(snapshotId, List.of()));
    }

    @Override
    public List<KnowledgeAnswerResolutionSnapshotBlockReference> findByBlock(KnowledgeAnswerResolutionStateBlockId blockId) {
        return List.copyOf(blockIndex.getOrDefault(blockId, List.of()));
    }

    @Override
    public void removeSnapshot(String snapshotId) {
        List<KnowledgeAnswerResolutionSnapshotBlockReference> removed = snapshotIndex.remove(snapshotId);
        if (removed != null) {
            for (var ref : removed) {
                List<KnowledgeAnswerResolutionSnapshotBlockReference> list = blockIndex.get(ref.blockId());
                if (list != null) {
                    list.removeIf(r -> r.snapshotId().equals(snapshotId));
                }
            }
        }
    }
}
