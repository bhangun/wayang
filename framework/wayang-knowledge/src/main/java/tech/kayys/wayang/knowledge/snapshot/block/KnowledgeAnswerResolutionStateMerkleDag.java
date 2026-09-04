package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.List;
import java.util.Map;

public record KnowledgeAnswerResolutionStateMerkleDag(
        String rootHash,
        List<KnowledgeAnswerResolutionStateMerkleNode> nodes,
        Map<String, KnowledgeAnswerResolutionStateMerkleNode> nodeIndex
) {
    public KnowledgeAnswerResolutionStateMerkleDag {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        nodeIndex = nodeIndex == null ? Map.of() : Map.copyOf(nodeIndex);
    }
}
