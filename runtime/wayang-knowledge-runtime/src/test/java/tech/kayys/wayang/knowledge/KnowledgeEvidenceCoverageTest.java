package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.coverage.*;
import tech.kayys.wayang.knowledge.exchange.routing.KnowledgeEvidenceFederatedRetrievalStrategy;
import tech.kayys.wayang.knowledge.exchange.routing.KnowledgeEvidenceSemanticQuery;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceCoverageTest {

    @Test
    public void testQuestionAspectAnalyzer() {
        DefaultKnowledgeEvidenceQuestionAspectAnalyzer analyzer =
                new DefaultKnowledgeEvidenceQuestionAspectAnalyzer();

        KnowledgeEvidenceSemanticQuery query = new KnowledgeEvidenceSemanticQuery(
                "q-1", "How to configure SSL and certificates?",
                "tenant-1", "ws-1", "proj-1", "agent-1",
                10, 0.6, Instant.now(), List.of(), List.of(),
                KnowledgeEvidenceFederatedRetrievalStrategy.BROADCAST,
                true, true, Map.of()
        );

        List<KnowledgeEvidenceQuestionAspect> aspects = analyzer.analyze(query);
        assertNotNull(aspects);
        assertFalse(aspects.isEmpty());
    }
}
