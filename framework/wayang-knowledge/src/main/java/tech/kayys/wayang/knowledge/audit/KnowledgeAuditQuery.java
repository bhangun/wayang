package tech.kayys.wayang.knowledge.audit;

import java.time.Instant;

public record KnowledgeAuditQuery(
        String tenantId,
        String workspaceId,
        String projectId,
        String agentId,
        String executionId,
        String operation,
        Instant from,
        Instant until,
        int limit
) {

    public KnowledgeAuditQuery {
        if (limit <= 0) {
            limit = 100;
        }
    }
}
