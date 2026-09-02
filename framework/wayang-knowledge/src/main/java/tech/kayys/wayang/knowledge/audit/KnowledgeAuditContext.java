package tech.kayys.wayang.knowledge.audit;

import java.util.Map;

public record KnowledgeAuditContext(
        String actorId,
        String tenantId,
        String workspaceId,
        String projectId,
        Map<String, Object> metadata
) {

    public KnowledgeAuditContext {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
