package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VersionedKnowledgeStoreTest {

    @Test
    void testVersionHistoryAndPointInTimeRetrieval() {
        VersionedKnowledgeStore store = new InMemoryVersionedKnowledgeStore();
        KnowledgeProvenance prov = KnowledgeProvenance.of("db://rules");

        KnowledgeItem v1 = new KnowledgeItem("rule-1", "src-1", "rule", "Rule 1", "Initial version", null, prov,
                KnowledgeAuthority.informational(), KnowledgeValidity.range(Instant.now().minusSeconds(100), Instant.now().minusSeconds(50)));
        store.save(v1).toCompletableFuture().join();

        KnowledgeItem v2 = new KnowledgeItem("rule-1", "src-1", "rule", "Rule 1", "Updated version", null, prov,
                KnowledgeAuthority.informational(), KnowledgeValidity.active());
        store.save(v2).toCompletableFuture().join();

        Optional<KnowledgeItem> latest = store.get("rule-1").toCompletableFuture().join();
        assertTrue(latest.isPresent());
        assertEquals("Updated version", latest.get().content());
        assertEquals(2L, latest.get().revision());

        Optional<KnowledgeItem> historical = store.getAsOf("rule-1", Instant.now().minusSeconds(75)).toCompletableFuture().join();
        assertTrue(historical.isPresent());
        assertEquals("Initial version", historical.get().content());
    }
}
