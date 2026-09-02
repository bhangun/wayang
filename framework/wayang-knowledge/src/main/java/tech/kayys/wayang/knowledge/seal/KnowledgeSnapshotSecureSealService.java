package tech.kayys.wayang.knowledge.seal;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityResult;

public interface KnowledgeSnapshotSecureSealService {

    KnowledgeSnapshotSecureSeal seal(
            KnowledgeSnapshotIntegrityResult result,
            String verifierId,
            String verifierVersion
    );
}
