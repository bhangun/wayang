package tech.kayys.wayang.knowledge.snapshot.pack;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityResult;
import tech.kayys.wayang.knowledge.seal.KnowledgeSnapshotSecureSeal;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;

public interface KnowledgeSnapshotVerificationManifestFactory {
    KnowledgeSnapshotVerificationManifest create(
            KnowledgeDecisionSnapshot snapshot,
            KnowledgeSnapshotIntegrityResult integrity,
            KnowledgeSnapshotSecureSeal seal
    );
}
