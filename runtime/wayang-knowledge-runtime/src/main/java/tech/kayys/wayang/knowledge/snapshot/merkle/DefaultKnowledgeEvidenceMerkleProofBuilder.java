package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultKnowledgeEvidenceMerkleProofBuilder
        implements KnowledgeEvidenceMerkleProofBuilder {

    @Override
    public KnowledgeEvidenceMerkleProof build(KnowledgeEvidenceMerkleTree tree, String leafId) {
        KnowledgeEvidenceMerkleLeaf leaf = tree.leaves()
                .stream()
                .filter(item -> item.leafId().equals(leafId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Merkle leaf not found: " + leafId));

        String current = tree.nodes()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().leaf() && leaf.artifactId().equals(entry.getValue().artifactId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Merkle node for leaf not found: " + leafId));

        String leafHash = current;
        List<KnowledgeEvidenceMerkleProofStep> steps = new ArrayList<>();

        while (!current.equals(tree.rootHash())) {
            String parentHash = findParent(tree, current);
            KnowledgeEvidenceMerkleNode parent = tree.nodes().get(parentHash);

            if (parent.leftHash().equals(current)) {
                steps.add(new KnowledgeEvidenceMerkleProofStep(
                        parent.rightHash(),
                        KnowledgeEvidenceMerkleProofStep.Direction.RIGHT
                ));
            } else {
                steps.add(new KnowledgeEvidenceMerkleProofStep(
                        parent.leftHash(),
                        KnowledgeEvidenceMerkleProofStep.Direction.LEFT
                ));
            }
            current = parentHash;
        }

        return new KnowledgeEvidenceMerkleProof(
                leafId,
                leafHash,
                tree.rootHash(),
                steps,
                Map.of()
        );
    }

    private String findParent(KnowledgeEvidenceMerkleTree tree, String child) {
        return tree.nodes()
                .entrySet()
                .stream()
                .filter(entry -> {
                    KnowledgeEvidenceMerkleNode node = entry.getValue();
                    return !node.leaf() && (child.equals(node.leftHash()) || child.equals(node.rightHash()));
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Merkle parent not found for " + child));
    }
}
