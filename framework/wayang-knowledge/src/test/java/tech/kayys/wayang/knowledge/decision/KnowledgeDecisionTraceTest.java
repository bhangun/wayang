package tech.kayys.wayang.knowledge.decision;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeDecisionTraceTest {

    @Test
    void testRecorderAndStore() {
        InMemoryKnowledgeDecisionTraceStore store = new InMemoryKnowledgeDecisionTraceStore();
        DefaultKnowledgeDecisionTraceService service = new DefaultKnowledgeDecisionTraceService(store);

        KnowledgeDecisionRecorder recorder = new KnowledgeDecisionRecorder("trace-123")
                .executionId("exec-001")
                .agentId("agent-alpha")
                .operation("REASONING")
                .query("What is the refund policy?")
                .evidence("ev-1")
                .policy("pol-1")
                .rule("rule-1")
                .step(KnowledgeDecisionSteps.governance("gov-check", List.of("ev-1"), KnowledgeDecisionStatus.ALLOWED, "Scope matched"))
                .step(KnowledgeDecisionSteps.policy("pol-1", KnowledgeDecisionStatus.ALLOWED, "Policy valid"))
                .outcome(KnowledgeDecisionOutcome.allowed("OK", "Refund eligible"));

        KnowledgeDecisionTrace trace = recorder.build();
        service.record(trace);

        assertTrue(trace.allowed());
        assertFalse(trace.denied());
        assertEquals(2, trace.steps().size());

        var fetched = service.get("trace-123");
        assertTrue(fetched.isPresent());
        assertEquals("exec-001", fetched.get().executionId());
        assertEquals("agent-alpha", fetched.get().agentId());
        assertEquals(1, service.findByExecution("exec-001").size());
        assertEquals(1, service.findByAgent("agent-alpha").size());
        assertEquals(1, service.findByEvidence("ev-1").size());
    }
}
