package tech.kayys.wayang.knowledge.snapshot.pack;

public interface KnowledgeSnapshotEvidencePackageVerifier {

    KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage
    );

    default KnowledgeSnapshotPackageVerificationResult verify(
            KnowledgeSnapshotEvidencePackage evidencePackage,
            KnowledgeSnapshotPackageVerificationContext context
    ) {
        return verify(evidencePackage);
    }
}
