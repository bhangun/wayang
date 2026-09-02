package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeResolverTest {

    static class MockKnowledgeSource implements KnowledgeSource {
        private final String id;
        private final List<KnowledgeEvidence> canned;

        MockKnowledgeSource(String id, List<KnowledgeEvidence> canned) {
            this.id = id;
            this.canned = canned;
        }

        @Override public String id() { return id; }
        @Override public CompletionStage<KnowledgeResult> query(KnowledgeQuery query, KnowledgeContext context) {
            return CompletableFuture.completedFuture(new KnowledgeResult(query, canned, java.util.Map.of(), Instant.now()));
        }
    }

    @Test
    void testMultiSourceResolutionAndRanking() {
        KnowledgeProvenance prov1 = KnowledgeProvenance.of("file:///docs/handbook.md");
        KnowledgeAuthority auth = KnowledgeAuthority.authoritative("policy", "HR", 1);
        KnowledgeItem item1 = new KnowledgeItem("item-1", "src-1", "policy", "Expense Policy", "Expense limit is $1000", null, prov1, auth, KnowledgeValidity.active());
        KnowledgeItem item2 = new KnowledgeItem("item-2", "src-2", "note", "Travel Note", "Always fly economy", null, prov1, KnowledgeAuthority.informational(), KnowledgeValidity.active());

        KnowledgeEvidence ev1 = new KnowledgeEvidence(item1, 0.95, "vector", java.util.Map.of());
        KnowledgeEvidence ev2 = new KnowledgeEvidence(item2, 0.70, "bm25", java.util.Map.of());

        KnowledgeRegistry registry = new DefaultKnowledgeRegistry();
        registry.register(new MockKnowledgeSource("src-1", List.of(ev1)));
        registry.register(new MockKnowledgeSource("src-2", List.of(ev2)));

        KnowledgeResolver resolver = new DefaultKnowledgeResolver(registry);
        KnowledgeQuery query = KnowledgeQuery.of("travel expenses").withTopK(5);
        KnowledgeResult result = resolver.resolve(query, KnowledgeContext.empty()).toCompletableFuture().join();

        assertNotNull(result);
        assertEquals(2, result.evidence().size());
        assertEquals("item-1", result.evidence().get(0).item().id());
        assertEquals("item-2", result.evidence().get(1).item().id());
    }

    @Test
    void testTemporalValidityFiltering() {
        KnowledgeProvenance prov = KnowledgeProvenance.of("file:///law.md");
        KnowledgeValidity expired = new KnowledgeValidity(Instant.now().minusSeconds(1000), Instant.now().minusSeconds(500), "EXPIRED");
        KnowledgeItem oldItem = new KnowledgeItem("item-old", "src-1", "law", "Old Law", "Old text", null, prov, KnowledgeAuthority.informational(), expired);

        KnowledgeEvidence ev = new KnowledgeEvidence(oldItem, 0.99, "vector", java.util.Map.of());
        KnowledgeRegistry registry = new DefaultKnowledgeRegistry();
        registry.register(new MockKnowledgeSource("src-1", List.of(ev)));

        KnowledgeResolver resolver = new DefaultKnowledgeResolver(registry);
        KnowledgeResult result = resolver.resolve(KnowledgeQuery.of("law"), KnowledgeContext.empty()).toCompletableFuture().join();

        assertTrue(result.evidence().isEmpty(), "Expired knowledge must be filtered out");
    }
}
