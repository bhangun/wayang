package tech.kayys.wayang.knowledge.seal;

public interface KnowledgeSnapshotSealVerificationService {

    KnowledgeSnapshotSealVerificationResult verify(
            KnowledgeSnapshotSecureSeal seal,
            KnowledgeSnapshotSealPayload payload
    );
}
