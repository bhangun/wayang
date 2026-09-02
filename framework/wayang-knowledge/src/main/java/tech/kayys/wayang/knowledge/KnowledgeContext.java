package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Execution-scoped knowledge context.
 */
public record KnowledgeContext(
        String tenantId,
        String userId,
        String agentId,
        String sessionId,
        String domain,
        String scope,
        Instant asOf,
        List<String> sourceIds,
        Map<String, Object> attributes
) {

    public KnowledgeContext {
        sourceIds = sourceIds == null ? List.of() : List.copyOf(sourceIds);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        scope = scope == null || scope.isBlank() ? "default" : scope;
        asOf = asOf == null ? Instant.now() : asOf;
    }

    public static KnowledgeContext empty() {
        return new KnowledgeContext(null, null, null, null, null, "default", Instant.now(), List.of(), Map.of());
    }

    public KnowledgeContext withSource(String sourceId) {
        var updated = new ArrayList<>(sourceIds);
        if (!updated.contains(sourceId)) {
            updated.add(sourceId);
        }
        return new KnowledgeContext(tenantId, userId, agentId, sessionId, domain, scope, asOf, updated, attributes);
    }

    public KnowledgeContext withDomain(String newDomain) {
        return new KnowledgeContext(tenantId, userId, agentId, sessionId, newDomain, scope, asOf, sourceIds, attributes);
    }
}
