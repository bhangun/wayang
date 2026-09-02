package tech.kayys.wayang.knowledge.snapshot;

import java.util.Map;

public record KnowledgeGovernanceSnapshot(
        String tenantId,
        String workspaceId,
        String projectId,
        String userId,
        String effectiveAt,
        String scopeFingerprint,
        String governancePolicyFingerprint,
        Map<String, Object> attributes
) {

    public KnowledgeGovernanceSnapshot {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
