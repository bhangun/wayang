package tech.kayys.wayang.knowledge.snapshot.merkle;

public interface KnowledgeEvidencePartialVerificationService {
    KnowledgeEvidencePartialVerificationResult verify(KnowledgeEvidenceMerkleProof proof);
}
