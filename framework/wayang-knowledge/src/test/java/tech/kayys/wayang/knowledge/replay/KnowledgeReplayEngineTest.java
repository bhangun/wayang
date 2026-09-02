package tech.kayys.wayang.knowledge.replay;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.decision.*;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeReplayEngineTest {

    @Test
    void testExactAndDivergedReplay() {
        InMemoryKnowledgeDecisionTraceStore traceStore = new InMemoryKnowledgeDecisionTraceStore();
        DefaultKnowledgeDecisionTraceService traceService = new DefaultKnowledgeDecisionTraceService(traceStore);

        KnowledgeDecisionTrace original = new KnowledgeDecisionRecorder("trace-replay-1")
                .executionId("exec-01")
                .agentId("agent-beta")
                .operation("EVALUATE")
                .evidence("ev-10")
                .policy("pol-20")
                .rule("rule-30")
                .outcome(KnowledgeDecisionOutcome.allowed("OK", "Approved"))
                .build();

        traceService.record(original);

        // Exact reexecutor returns identical trace
        KnowledgeDecisionReexecutor exactExecutor = (snapshot, context) -> original;
        KnowledgeReplayContextProvider contextProvider = new DefaultKnowledgeReplayContextProvider();
        DefaultKnowledgeReplayEngine engine = new DefaultKnowledgeReplayEngine(traceService, exactExecutor, contextProvider);

        KnowledgeReplayResult result = engine.replay(KnowledgeReplayRequest.exact("trace-replay-1"));
        assertTrue(result.reproduced());
        assertFalse(result.diverged());
        assertTrue(result.divergences().isEmpty());

        // Divergent reexecutor returns different outcome & policy
        KnowledgeDecisionTrace modified = new KnowledgeDecisionRecorder("trace-replay-1")
                .executionId("exec-01")
                .agentId("agent-beta")
                .operation("EVALUATE")
                .evidence("ev-10")
                .policy("pol-999") // Changed policy
                .rule("rule-30")
                .outcome(KnowledgeDecisionOutcome.denied("DENIED", "Policy changed"))
                .build();

        KnowledgeDecisionReexecutor divergentExecutor = (snapshot, context) -> modified;
        DefaultKnowledgeReplayEngine divEngine = new DefaultKnowledgeReplayEngine(traceService, divergentExecutor, contextProvider);

        KnowledgeReplayResult divResult = divEngine.replay(KnowledgeReplayRequest.exact("trace-replay-1"));
        assertFalse(divResult.reproduced());
        assertTrue(divResult.diverged());
        assertTrue(divResult.divergences().contains("policies"));
        assertTrue(divResult.divergences().contains("outcome"));
    }
}
