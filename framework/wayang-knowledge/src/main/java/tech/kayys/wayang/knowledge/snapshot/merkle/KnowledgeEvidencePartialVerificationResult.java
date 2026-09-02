package tech.kayys.wayang.knowledge.snapshot.merkle;

import java.util.List;
import java.util.Map;

public record KnowledgeEvidencePartialVerificationResult(
        boolean verified,
        String rootHash,
        String leafId,
        List<String> verifiedArtifacts,
        List<String> issues,
        Map<String, String> metadata
) {
    public KnowledgeEvidencePartialVerificationResult {
        verifiedArtifacts = verifiedArtifacts == null ? List.of() : List.copyOf(verifiedArtifacts);
        issues = issues == null ? List.of() : List.copyOf(issues);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
