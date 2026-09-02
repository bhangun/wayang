package tech.kayys.wayang.knowledge.replay;

import java.time.Instant;
import java.util.Map;

public record KnowledgeReplayContext(
        String tenantId,
        String workspaceId,
        String projectId,
        Instant effectiveAt,
        Map<String, Object> attributes
) {

    public KnowledgeReplayContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
