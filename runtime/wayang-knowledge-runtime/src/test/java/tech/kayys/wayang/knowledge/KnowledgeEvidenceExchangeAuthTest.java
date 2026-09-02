package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeRequest;
import tech.kayys.wayang.knowledge.exchange.auth.*;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEvidenceExchangeAuthTest {

    @Test
    void testTenantAndScopeIsolation() {
        CompositeKnowledgeEvidenceExchangeAuthorizer authorizer = new CompositeKnowledgeEvidenceExchangeAuthorizer(List.of(
                new KnowledgeEvidenceTenantIsolationPolicy(),
                new KnowledgeEvidenceScopeIsolationPolicy(),
                new KnowledgeEvidenceExchangeOperationPolicy()
        ));

        KnowledgeEvidenceExchangePrincipal principal = new KnowledgeEvidenceExchangePrincipal(
                "p-1", "runtime-1", "agent-1", "user-1",
                "tenant-A", "ws-1", "proj-1", "user-1",
                Set.of("evidence-reader"), Map.of()
        );

        KnowledgeEvidenceExchangeRequest validReq = new KnowledgeEvidenceExchangeRequest(
                KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT,
                new KnowledgeEvidenceArtifactId("sha256", "abc"),
                null, null, null, false, false, true,
                Map.of("tenantId", "tenant-A", "workspaceId", "ws-1", "projectId", "proj-1")
        );

        KnowledgeEvidenceExchangeAuthorizationContext validContext =
                new KnowledgeEvidenceExchangeAuthorizationContext(validReq, principal, Instant.now(), Map.of());

        KnowledgeEvidenceExchangeAuthorizationDecision decision = authorizer.authorize(validContext);
        assertTrue(decision instanceof KnowledgeEvidenceExchangeAuthorizationDecision.Allow);

        // Cross-tenant attempt
        KnowledgeEvidenceExchangeRequest crossTenantReq = new KnowledgeEvidenceExchangeRequest(
                KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT,
                new KnowledgeEvidenceArtifactId("sha256", "abc"),
                null, null, null, false, false, true,
                Map.of("tenantId", "tenant-B", "workspaceId", "ws-1", "projectId", "proj-1")
        );
        KnowledgeEvidenceExchangeAuthorizationContext crossContext =
                new KnowledgeEvidenceExchangeAuthorizationContext(crossTenantReq, principal, Instant.now(), Map.of());

        KnowledgeEvidenceExchangeAuthorizationDecision denyDecision = authorizer.authorize(crossContext);
        assertTrue(denyDecision instanceof KnowledgeEvidenceExchangeAuthorizationDecision.Deny);
    }
}
