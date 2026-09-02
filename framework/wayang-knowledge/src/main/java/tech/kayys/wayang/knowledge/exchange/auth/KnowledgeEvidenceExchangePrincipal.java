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

    public static KnowledgeEvidenceExchangePrincipal of(String principalId, String runtimeId, String tenantId) {
        return new KnowledgeEvidenceExchangePrincipal(
                principalId,
                runtimeId,
                null,
                null,
                tenantId,
                null,
                null,
                null,
                Set.of(),
                Map.of()
        );
    }
}
