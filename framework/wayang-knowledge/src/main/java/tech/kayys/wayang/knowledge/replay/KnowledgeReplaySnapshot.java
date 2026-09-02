package tech.kayys.wayang.knowledge.replay;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeReplaySnapshot(
        String traceId,
        String executionId,
        String agentId,
        String operation,
        String query,
        Instant effectiveAt,
        List<String> evidenceIds,
        List<String> evidenceVersionIds,
        List<String> lineageIds,
        List<String> policyIds,
        List<String> ruleIds,
        String governanceFingerprint,
        String policyFingerprint,
        String evidenceFingerprint,
        String configurationFingerprint,
        Map<String, Object> metadata
) {

    public KnowledgeReplaySnapshot {
        evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
        evidenceVersionIds = evidenceVersionIds == null ? List.of() : List.copyOf(evidenceVersionIds);
        lineageIds = lineageIds == null ? List.of() : List.copyOf(lineageIds);
        policyIds = policyIds == null ? List.of() : List.copyOf(policyIds);
        ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
