package tech.kayys.wayang.knowledge.snapshot;

import java.util.List;
import java.util.Map;

public record KnowledgePolicySnapshot(
        List<KnowledgeVersionReference> policies,
        String aggregateFingerprint,
        Map<String, Object> metadata
) {

    public KnowledgePolicySnapshot {
        policies = policies == null ? List.of() : List.copyOf(policies);
        aggregateFingerprint = aggregateFingerprint == null ? "" : aggregateFingerprint;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public List<KnowledgeVersionReference> references() {
        return policies;
    }
}
