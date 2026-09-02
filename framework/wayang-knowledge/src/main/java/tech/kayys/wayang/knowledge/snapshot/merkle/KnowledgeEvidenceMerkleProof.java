package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.List;
import java.util.Map;

public record KnowledgeEvidenceMerkleProof(
        String leafId,
        String leafHash,
        String rootHash,
        List<KnowledgeEvidenceMerkleProofStep> steps,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceMerkleProof {
        steps = steps == null ? List.of() : List.copyOf(steps);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
