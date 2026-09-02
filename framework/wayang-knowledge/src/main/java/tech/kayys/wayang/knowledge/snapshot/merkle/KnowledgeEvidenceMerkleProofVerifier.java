package tech.kayys.wayang.knowledge.snapshot.merkle;

public interface KnowledgeEvidenceMerkleProofVerifier {
    boolean verify(KnowledgeEvidenceMerkleProof proof);
}
