package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.snapshot.merkle.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceMerkleTest {

    @Test
    void testMerkleTreeAndPartialVerification() {
        KnowledgeEvidenceMerkleLeaf leaf1 = new KnowledgeEvidenceMerkleLeaf("leaf-1", "art-1", "knowledge", "hash-1", 100, Map.of());
        KnowledgeEvidenceMerkleLeaf leaf2 = new KnowledgeEvidenceMerkleLeaf("leaf-2", "art-2", "policy", "hash-2", 200, Map.of());
        KnowledgeEvidenceMerkleLeaf leaf3 = new KnowledgeEvidenceMerkleLeaf("leaf-3", "art-3", "rule", "hash-3", 300, Map.of());

        DefaultKnowledgeEvidenceMerkleTreeBuilder treeBuilder = new DefaultKnowledgeEvidenceMerkleTreeBuilder();
        KnowledgeEvidenceMerkleTree tree = treeBuilder.build(List.of(leaf1, leaf2, leaf3));

        assertNotNull(tree.rootHash());
        assertEquals(3, tree.leaves().size());

        DefaultKnowledgeEvidenceMerkleProofBuilder proofBuilder = new DefaultKnowledgeEvidenceMerkleProofBuilder();
        KnowledgeEvidenceMerkleProof proof1 = proofBuilder.build(tree, "leaf-1");

        assertNotNull(proof1);
        assertEquals("leaf-1", proof1.leafId());
        assertEquals(tree.rootHash(), proof1.rootHash());

        DefaultKnowledgeEvidenceMerkleProofVerifier proofVerifier = new DefaultKnowledgeEvidenceMerkleProofVerifier();
        assertTrue(proofVerifier.verify(proof1));

        DefaultKnowledgeEvidencePartialVerificationService partialService =
                new DefaultKnowledgeEvidencePartialVerificationService(proofVerifier);
        KnowledgeEvidencePartialVerificationResult partialResult = partialService.verify(proof1);

        assertTrue(partialResult.verified());
        assertEquals(tree.rootHash(), partialResult.rootHash());
    }
}
