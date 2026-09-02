package tech.kayys.wayang.knowledge.snapshot;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeDecisionSnapshot(
        KnowledgeSnapshotId snapshotId,
        String executionId,
        String traceId,
        String agentId,
        String operation,
        String query,
        Instant effectiveAt,
        List<KnowledgeSnapshotEntry> knowledge,
        KnowledgePolicySnapshot policies,
        KnowledgeRuleSnapshot rules,
        KnowledgeGovernanceSnapshot governance,
        KnowledgeRuntimeSnapshot runtime,
        String aggregateFingerprint,
        Instant createdAt,
        Map<String, Object> metadata
) {

    public KnowledgeDecisionSnapshot {
        knowledge = knowledge == null ? List.of() : List.copyOf(knowledge);
        policies = policies == null ? new KnowledgePolicySnapshot(List.of(), "", Map.of()) : policies;
        rules = rules == null ? new KnowledgeRuleSnapshot(List.of(), "", Map.of()) : rules;
        governance = governance == null ? new KnowledgeGovernanceSnapshot(null, null, null, null, null, null, null, Map.of()) : governance;
        runtime = runtime == null ? new KnowledgeRuntimeSnapshot(null, null, null, null, null, null, null, null, Map.of()) : runtime;
        aggregateFingerprint = aggregateFingerprint == null ? "" : aggregateFingerprint;
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
