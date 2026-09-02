package tech.kayys.wayang.knowledge.seal;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeTrustAnchorStore implements KnowledgeTrustAnchorStore {

    private final Map<String, KnowledgeSnapshotExternalTrustAnchor> anchors = new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeSnapshotExternalTrustAnchor anchor) {
        anchors.put(anchor.anchorId(), anchor);
    }

    @Override
    public Optional<KnowledgeSnapshotExternalTrustAnchor> get(String anchorId) {
        return Optional.ofNullable(anchors.get(anchorId));
    }

    @Override
    public List<KnowledgeSnapshotExternalTrustAnchor> findBySnapshot(KnowledgeSnapshotId snapshotId) {
        return anchors.values().stream()
                .filter(a -> a.snapshotId().equals(snapshotId))
                .toList();
    }
}
