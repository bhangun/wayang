package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.List;

public interface KnowledgeEvidenceMerkleTreeBuilder {
    KnowledgeEvidenceMerkleTree build(List<KnowledgeEvidenceMerkleLeaf> leaves);
}
