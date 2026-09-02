package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.federation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceFederationTest {

    @Test
    public void testFederationDeduplicationAndRanking() {
        DefaultKnowledgeEvidenceFederationDeduplicator deduplicator =
                new DefaultKnowledgeEvidenceFederationDeduplicator();

        DefaultKnowledgeEvidenceFederationRanker ranker =
                new DefaultKnowledgeEvidenceFederationRanker();

        Instant now = Instant.now();
        KnowledgeEvidenceReference ref1 = new KnowledgeEvidenceReference(
                "know-1", "v1", "frag-1", "Excerpt 1", 0.9, 0.8, 0.95, Map.of()
        );
        KnowledgeEvidenceReference ref2 = new KnowledgeEvidenceReference(
                "know-1", "v1", "frag-1", "Excerpt 1 duplicate", 0.85, 0.8, 0.95, Map.of()
        );
        KnowledgeEvidenceReference ref3 = new KnowledgeEvidenceReference(
                "know-2", "v1", "frag-2", "Excerpt 2", 0.7, 0.6, 0.8, Map.of()
        );

        var deduplicated = deduplicator.deduplicate(List.of(ref1, ref2, ref3));
        assertNotNull(deduplicated);
        assertEquals(2, deduplicated.size());

        var query = new KnowledgeEvidenceFederatedQuery(
                "query-1", "tenant-1", "ws-1", "proj-1", "agent-1",
                "agent query text", 10, 0.5, now,
                true, true, true, Map.of(), Map.of()
        );

        var ranked = ranker.rank(deduplicated, query);
        assertNotNull(ranked);
        assertEquals(2, ranked.size());
        assertEquals("know-1", ranked.get(0).knowledgeId());
    }
}
