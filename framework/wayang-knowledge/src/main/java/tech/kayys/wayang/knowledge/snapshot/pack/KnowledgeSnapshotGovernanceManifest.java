package tech.kayys.wayang.knowledge.snapshot.pack;

import java.time.Instant;
import java.util.Map;

public record KnowledgeSnapshotGovernanceManifest(
        String tenantId,
        String workspaceId,
        String projectId,
        String userId,
        Instant effectiveAt,
        String scopeFingerprint,
        String governancePolicyFingerprint,
        Map<String, String> attributes
) {
    public KnowledgeSnapshotGovernanceManifest {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
