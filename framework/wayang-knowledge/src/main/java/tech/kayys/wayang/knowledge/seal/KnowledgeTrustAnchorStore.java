package tech.kayys.wayang.knowledge.seal;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;
import java.util.Optional;

public interface KnowledgeTrustAnchorStore {

    void save(KnowledgeSnapshotExternalTrustAnchor anchor);

    Optional<KnowledgeSnapshotExternalTrustAnchor> get(String anchorId);

    List<KnowledgeSnapshotExternalTrustAnchor> findBySnapshot(KnowledgeSnapshotId snapshotId);
}
