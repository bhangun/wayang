package tech.kayys.wayang.knowledge.snapshot.pack;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityResult;
import tech.kayys.wayang.knowledge.seal.KnowledgeSnapshotSecureSeal;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;

public interface KnowledgeSnapshotEvidencePackageService {

    KnowledgeSnapshotEvidencePackage create(
            KnowledgeDecisionSnapshot snapshot,
            KnowledgeSnapshotIntegrityResult integrity,
            KnowledgeSnapshotSecureSeal seal
    );

    KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage
    );

    KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage,
            KnowledgeSnapshotPackageVerificationContext context
    );
}
