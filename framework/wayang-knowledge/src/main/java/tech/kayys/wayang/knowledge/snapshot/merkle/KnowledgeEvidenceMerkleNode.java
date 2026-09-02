package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.Map;

public record KnowledgeEvidenceMerkleNode(
        String hash,
        String leftHash,
        String rightHash,
        boolean leaf,
        String artifactId,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceMerkleNode {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
