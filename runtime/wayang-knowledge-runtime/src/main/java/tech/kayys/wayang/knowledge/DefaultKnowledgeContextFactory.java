package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Builds a domain-neutral KnowledgeContext from execution parameters.
 */
public final class DefaultKnowledgeContextFactory {

    private DefaultKnowledgeContextFactory() {}

    public static KnowledgeContext create(
            String tenantId,
            String userId,
            String agentId,
            String sessionId,
            String domain,
            String scope,
            List<String> sourceIds,
            Map<String, Object> attributes
    ) {
        return new KnowledgeContext(
                tenantId,
                userId,
                agentId,
                sessionId,
                domain != null && !domain.isBlank() ? domain : "default",
                scope != null && !scope.isBlank() ? scope : "default",
                Instant.now(),
                sourceIds,
                attributes
        );
    }
}
