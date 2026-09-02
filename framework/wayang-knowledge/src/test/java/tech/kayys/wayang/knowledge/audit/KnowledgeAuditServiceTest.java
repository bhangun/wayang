package tech.kayys.wayang.knowledge.audit;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.decision.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeAuditServiceTest {

    @Test
    void testAuditPipelineAndQuery() {
        InMemoryKnowledgeAuditStore auditStore = new InMemoryKnowledgeAuditStore();
        KnowledgeAuditPolicy policy = new DefaultKnowledgeAuditPolicy();
        KnowledgeAuditRedactor redactor = new NoOpKnowledgeAuditRedactor();
        DefaultKnowledgeAuditService auditService = new DefaultKnowledgeAuditService(auditStore, policy, redactor);

        KnowledgeDecisionTrace trace = new KnowledgeDecisionRecorder("trace-99")
                .executionId("exec-77")
                .agentId("salam")
                .operation("DISPUTE_RESOLUTION")
                .outcome(KnowledgeDecisionOutcome.allowed("APPROVED", "Resolution approved"))
                .build();

        KnowledgeAuditContext context = new KnowledgeAuditContext(
                "user-1",
                "tenant-xyz",
                "ws-1",
                "proj-1",
                Map.of("category", "finance")
        );

        auditService.audit(trace, context);

        KnowledgeAuditQuery query = new KnowledgeAuditQuery(
                "tenant-xyz",
                "ws-1",
                "proj-1",
                "salam",
                "exec-77",
                "DISPUTE_RESOLUTION",
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(60),
                10
        );

        List<KnowledgeAuditEvent> events = auditService.query(query);
        assertEquals(1, events.size());
        assertEquals("tenant-xyz", events.get(0).tenantId());
        assertEquals("trace-99", events.get(0).decisionTrace().id());
    }
}
