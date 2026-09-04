package tech.kayys.wayang.knowledge.snapshot.block;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class KnowledgeAnswerResolutionStateMerkleDagBuilder {

    public KnowledgeAnswerResolutionStateMerkleDag build(List<KnowledgeAnswerResolutionStateBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            String emptyHash = hash("");
            KnowledgeAnswerResolutionStateMerkleNode root = new KnowledgeAnswerResolutionStateMerkleNode(
                    "node-" + emptyHash, emptyHash, List.of(), true, Map.of());
            return new KnowledgeAnswerResolutionStateMerkleDag(emptyHash, List.of(root), Map.of(root.nodeId(), root));
        }

        List<KnowledgeAnswerResolutionStateMerkleNode> allNodes = new ArrayList<>();
        Map<String, KnowledgeAnswerResolutionStateMerkleNode> nodeIndex = new HashMap<>();

        List<KnowledgeAnswerResolutionStateMerkleNode> currentLevel = new ArrayList<>();
        for (KnowledgeAnswerResolutionStateBlock block : blocks) {
            String leafHash = block.fingerprint();
            KnowledgeAnswerResolutionStateMerkleNode leaf = new KnowledgeAnswerResolutionStateMerkleNode(
                    block.blockId(),
                    leafHash,
                    List.of(),
                    true,
                    Map.of("sizeBytes", String.valueOf(block.sizeBytes()))
            );
            currentLevel.add(leaf);
            allNodes.add(leaf);
            nodeIndex.put(leaf.nodeId(), leaf);
        }

        while (currentLevel.size() > 1) {
            List<KnowledgeAnswerResolutionStateMerkleNode> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                KnowledgeAnswerResolutionStateMerkleNode left = currentLevel.get(i);
                KnowledgeAnswerResolutionStateMerkleNode right = (i + 1 < currentLevel.size())
                        ? currentLevel.get(i + 1)
                        : left;

                String parentHash = hash(left.hash() + ":" + right.hash());
                KnowledgeAnswerResolutionStateMerkleNode parent = new KnowledgeAnswerResolutionStateMerkleNode(
                        "node-" + parentHash,
                        parentHash,
                        List.of(left.nodeId(), right.nodeId()),
                        false,
                        Map.of()
                );
                nextLevel.add(parent);
                allNodes.add(parent);
                nodeIndex.put(parent.nodeId(), parent);
            }
            currentLevel = nextLevel;
        }

        KnowledgeAnswerResolutionStateMerkleNode root = currentLevel.get(0);
        return new KnowledgeAnswerResolutionStateMerkleDag(root.hash(), allNodes, nodeIndex);
    }

    private String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
