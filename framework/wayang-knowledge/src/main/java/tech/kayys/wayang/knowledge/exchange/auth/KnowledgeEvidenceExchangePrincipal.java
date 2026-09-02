package tech.kayys.wayang.knowledge.exchange.auth;

import java.util.Map;
import java.util.Set;

public record KnowledgeEvidenceExchangePrincipal(
        String principalId,
        String runtimeId,
        String agentId,
        String actorId,
        String tenantId,
        String workspaceId,
        String projectId,
        String userId,
        Set<String> roles,
        Map<String, String> attributes
) {
    public KnowledgeEvidenceExchangePrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
