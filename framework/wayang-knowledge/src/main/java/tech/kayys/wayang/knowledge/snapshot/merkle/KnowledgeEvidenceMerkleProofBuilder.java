package tech.kayys.wayang.knowledge.snapshot.merkle;

public interface KnowledgeEvidenceMerkleProofBuilder {
    KnowledgeEvidenceMerkleProof build(KnowledgeEvidenceMerkleTree tree, String leafId);
}
