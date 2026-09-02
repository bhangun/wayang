package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.federation.KnowledgeEvidenceFederationQueryResult;
import tech.kayys.wayang.knowledge.exchange.routing.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceRoutingTest {

    @Test
    public void testQueryAnalysisAndDecomposer() {
        DefaultKnowledgeEvidenceQueryAnalyzer analyzer =
                new DefaultKnowledgeEvidenceQueryAnalyzer();

        Instant now = Instant.now();
        KnowledgeEvidenceSemanticQuery query = new KnowledgeEvidenceSemanticQuery(
                "q-1", "Explain distributed snapshotting",
                "tenant-1", "ws-1", "proj-1", "agent-1",
                10, 0.6, now,
                List.of("SNAPSHOT", "STORAGE"),
                List.of("distrib"),
                KnowledgeEvidenceFederatedRetrievalStrategy.BROADCAST,
                true, true, Map.of()
        );

        var intent = analyzer.analyze(query);
        assertNotNull(intent);
        assertEquals(query.text(), intent.originalQuery());

        DefaultKnowledgeEvidenceQueryDecomposer decomposer =
                new DefaultKnowledgeEvidenceQueryDecomposer();

        var subQueries = decomposer.decompose(query, intent);
        assertNotNull(subQueries);
        assertFalse(subQueries.isEmpty());
    }

    @Test
    public void testScoreNormalizationAndFusion() {
        DefaultKnowledgeEvidenceScoreNormalizer normalizer =
                new DefaultKnowledgeEvidenceScoreNormalizer();

        double normalized = normalizer.normalize(0.85, "runtime-1");
        assertTrue(normalized >= 0.0 && normalized <= 1.0);

        DefaultKnowledgeEvidenceFusionService fusionService =
                new DefaultKnowledgeEvidenceFusionService();

        KnowledgeEvidenceReference ref1 = new KnowledgeEvidenceReference(
                "know-1", "v1", "frag-1", "Excerpt 1", 0.9, 0.8, 0.95, Map.of()
        );
        KnowledgeEvidenceReference ref2 = new KnowledgeEvidenceReference(
                "know-2", "v1", "frag-2", "Excerpt 2", 0.7, 0.6, 0.8, Map.of()
        );

        var result1 = new KnowledgeEvidenceFederationQueryResult(
                "q-1", "runtime-1", List.of(ref1), true, true, "SUCCESS", Map.of()
        );
        var result2 = new KnowledgeEvidenceFederationQueryResult(
                "q-1", "runtime-2", List.of(ref2), true, true, "SUCCESS", Map.of()
        );

        var fused = fusionService.fuse(List.of(result1, result2));
        assertNotNull(fused);
        assertEquals(2, fused.size());
    }
}
