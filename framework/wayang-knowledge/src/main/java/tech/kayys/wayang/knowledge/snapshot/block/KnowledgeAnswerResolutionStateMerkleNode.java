package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.List;
import java.util.Map;

public record KnowledgeAnswerResolutionStateMerkleNode(
        String nodeId,
        String hash,
        List<String> children,
        boolean leaf,
        Map<String, String> metadata
) {
    public KnowledgeAnswerResolutionStateMerkleNode {
        children = children == null ? List.of() : List.copyOf(children);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
