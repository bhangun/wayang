package tech.kayys.wayang.security.enforcement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.security.authz.AuthorizationDecision;
import tech.kayys.wayang.security.authz.AuthorizationService;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.obligation.*;
import tech.kayys.wayang.security.tenant.TenantContext;
import tech.kayys.wayang.security.transform.DefaultDataTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class PolicyEnforcementPointTest {

    private DefaultObligationRegistry registry;
    private DefaultObligationPipeline pipeline;
    private DefaultDataTransformer transformer;

    @BeforeEach
    void setUp() {
        registry = new DefaultObligationRegistry();
        registry.register(tech.kayys.wayang.security.transform.TransformObligationExecutor.of(StandardObligations.REDACT));
        registry.register(tech.kayys.wayang.security.transform.TransformObligationExecutor.of(StandardObligations.MASK));
        pipeline = new DefaultObligationPipeline(registry);
        transformer = new DefaultDataTransformer();
    }

    @Test
    void testEnforcementAllowsUntouchedDataWhenNoObligations() {
        AuthorizationService authService = req -> CompletableFuture.completedFuture(
                AuthorizationDecision.allow("granted")
        );

        DefaultPolicyEnforcementPoint pep = new DefaultPolicyEnforcementPoint(authService, pipeline, transformer);

        Principal principal = Principal.agent("agent-a", "Agent A");
        SecurityContext secCtx = SecurityContext.of(principal, TenantContext.of("tenant-1"));

        Map<String, Object> payload = Map.of("title", "Hello", "value", 42);
        EnforcementRequest request = EnforcementRequest.of(secCtx, "data.read", payload);

        EnforcementResult result = pep.enforce(request).toCompletableFuture().join();

        assertNotNull(result);
        assertTrue(result.allowed());
        assertEquals(payload, result.data());
    }

    @Test
    void testEnforcementDeniesWhenAuthorizationRejects() {
        AuthorizationService authService = req -> CompletableFuture.completedFuture(
                AuthorizationDecision.deny("Unauthorized capability")
        );

        DefaultPolicyEnforcementPoint pep = new DefaultPolicyEnforcementPoint(authService, pipeline, transformer);

        Principal principal = Principal.anonymous();
        SecurityContext secCtx = SecurityContext.of(principal, TenantContext.empty());

        EnforcementRequest request = EnforcementRequest.of(secCtx, "admin.delete", Map.of("key", "val"));
        EnforcementResult result = pep.enforce(request).toCompletableFuture().join();

        assertNotNull(result);
        assertFalse(result.allowed());
        assertTrue(result.reason().contains("Unauthorized capability"));
    }

    @Test
    void testEnforcementAppliesRedactionObligation() {
        // Obligation to redact "password" and "ssn"
        Obligation redactOb = Obligation.of(
                StandardObligations.REDACT,
                ObligationPhase.DURING_EXECUTION,
                Map.of("fields", List.of("password", "ssn"))
        );

        AuthorizationService authService = req -> CompletableFuture.completedFuture(
                new AuthorizationDecision(true, "allow-with-redaction", Map.of("obligations", List.of(redactOb)))
        );

        DefaultPolicyEnforcementPoint pep = new DefaultPolicyEnforcementPoint(authService, pipeline, transformer);

        Principal principal = Principal.agent("customer-agent", "Customer Agent");
        SecurityContext secCtx = SecurityContext.of(principal, TenantContext.of("tenant-1"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "alice");
        userData.put("password", "secret123");
        userData.put("ssn", "000-11-2222");
        userData.put("role", "developer");

        EnforcementRequest request = EnforcementRequest.of(secCtx, "user.get", userData);
        EnforcementResult result = pep.enforce(request).toCompletableFuture().join();

        assertNotNull(result);
        assertTrue(result.allowed());

        @SuppressWarnings("unchecked")
        Map<String, Object> transformed = (Map<String, Object>) result.data();
        assertNotNull(transformed);
        assertEquals("alice", transformed.get("username"));
        assertEquals("developer", transformed.get("role"));
        assertFalse(transformed.containsKey("password"), "password should have been redacted");
        assertFalse(transformed.containsKey("ssn"), "ssn should have been redacted");
    }
}
