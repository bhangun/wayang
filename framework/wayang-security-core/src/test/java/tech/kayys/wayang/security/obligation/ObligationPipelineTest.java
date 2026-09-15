package tech.kayys.wayang.security.obligation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.obligation.audit.AuditObligationExecutor;
import tech.kayys.wayang.security.obligation.audit.InMemoryAuditSink;
import tech.kayys.wayang.security.obligation.ratelimit.InMemoryRateLimiter;
import tech.kayys.wayang.security.obligation.ratelimit.RateLimitObligationExecutor;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObligationPipelineTest {

    private DefaultObligationRegistry registry;
    private DefaultObligationPipeline pipeline;
    private InMemoryAuditSink auditSink;
    private InMemoryRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        registry = new DefaultObligationRegistry();
        auditSink = new InMemoryAuditSink();
        rateLimiter = new InMemoryRateLimiter(2); // allow up to 2 permits

        registry.register(new AuditObligationExecutor(auditSink));
        registry.register(new RateLimitObligationExecutor(rateLimiter));

        pipeline = new DefaultObligationPipeline(registry);
    }

    @Test
    void testSequentialObligationExecution() {
        Obligation auditOb = Obligation.before(StandardObligations.AUDIT);
        Obligation rateOb = Obligation.of(StandardObligations.RATE_LIMIT, ObligationPhase.BEFORE_EXECUTION, Map.of("key", "test-user", "permits", 1));

        Principal principal = Principal.agent("agent-1", "Agent 1");
        SecurityContext secCtx = SecurityContext.of(principal, TenantContext.of("tenant-a"));
        ObligationContext context = ObligationContext.of(secCtx, Map.of("capability", "file.read", "action", "read"));

        ObligationPipelineResult result = pipeline.execute(List.of(auditOb, rateOb), context)
                .toCompletableFuture()
                .join();

        assertNotNull(result);
        assertTrue(result.successful());
        assertTrue(result.continueExecution());
        assertEquals(2, result.results().size());

        // Verify audit sink received event
        assertEquals(1, auditSink.events().size());
        assertEquals("read", auditSink.events().get(0).action());
    }

    @Test
    void testFailClosedOnUnhandledObligation() {
        Obligation unhandled = Obligation.before(ObligationType.of("unregistered-obligation"));

        Principal principal = Principal.anonymous();
        ObligationContext context = ObligationContext.of(SecurityContext.of(principal, TenantContext.empty()));

        ObligationPipelineResult result = pipeline.execute(List.of(unhandled), context)
                .toCompletableFuture()
                .join();

        assertNotNull(result);
        assertFalse(result.successful());
        assertFalse(result.continueExecution());
        assertTrue(result.attributes().containsKey("error"));
        assertTrue(result.attributes().get("error").toString().contains("No executor registered"));
    }

    @Test
    void testRateLimitExceededStopsExecution() {
        Obligation rateOb = Obligation.of(StandardObligations.RATE_LIMIT, ObligationPhase.BEFORE_EXECUTION, Map.of("key", "quota-key", "permits", 2));
        Principal principal = Principal.agent("agent-1", "Agent 1");
        ObligationContext context = ObligationContext.of(SecurityContext.of(principal, TenantContext.empty()));

        // First call consumes 2 permits (limit is 2)
        ObligationPipelineResult r1 = pipeline.execute(List.of(rateOb), context).toCompletableFuture().join();
        assertTrue(r1.successful());
        assertTrue(r1.continueExecution());

        // Second call exceeds permits
        ObligationPipelineResult r2 = pipeline.execute(List.of(rateOb), context).toCompletableFuture().join();
        assertFalse(r2.successful());
        assertFalse(r2.continueExecution());
    }
}
