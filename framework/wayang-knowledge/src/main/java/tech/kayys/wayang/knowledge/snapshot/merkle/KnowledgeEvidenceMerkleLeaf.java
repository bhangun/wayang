package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.Map;

public record KnowledgeEvidenceMerkleLeaf(
        String leafId,
        String artifactId,
        String resourceType,
        String contentHash,
        long size,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceMerkleLeaf {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
