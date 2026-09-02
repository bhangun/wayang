package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.resolution.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerResolutionTest {

    @Test
    public void testAnswerResolutionPolicy() {
        DefaultKnowledgeAnswerResolutionPolicy policy =
                new DefaultKnowledgeAnswerResolutionPolicy();

        KnowledgeAnswerResolutionCandidate cand1 = new KnowledgeAnswerResolutionCandidate(
                "cand-1", "runtime-1", 0.95, 0.9, 0.9, 0.9, 0.9, 0.9, 0.0, 0.92,
                true, true, Map.of()
        );

        KnowledgeAnswerResolutionContext context = new KnowledgeAnswerResolutionContext(
                "tenant-1", "ws-1", "proj-1", "exec-1", Instant.now(), true, false, Map.of()
        );

        var decision = policy.decide(List.of(cand1), List.of(), context);
        assertNotNull(decision);
        assertEquals(KnowledgeAnswerResolutionDecision.PREFER, decision);
    }
}
