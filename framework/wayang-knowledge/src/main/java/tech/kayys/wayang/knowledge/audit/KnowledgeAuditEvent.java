package tech.kayys.wayang.knowledge.audit;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.time.Instant;
import java.util.Map;

/**
 * Generic audit event emitted from governed knowledge execution.
 */
public record KnowledgeAuditEvent(
        String id,
        String executionId,
        String agentId,
        String actorId,
        String tenantId,
        String workspaceId,
        String projectId,
        String operation,
        KnowledgeDecisionTrace decisionTrace,
        Instant createdAt,
        Map<String, Object> metadata
) {

    public KnowledgeAuditEvent {
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
