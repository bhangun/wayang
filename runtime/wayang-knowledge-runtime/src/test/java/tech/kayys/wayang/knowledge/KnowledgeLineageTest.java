package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.lineage.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeLineageTest {

    @Test
    void testLineageTraceability() {
        KnowledgeLineageStore store = new InMemoryKnowledgeLineageStore();
        KnowledgeLineageService service = new DefaultKnowledgeLineageService(store);

        KnowledgeLineageEdge edge1 = KnowledgeLineageEdge.derivation("doc-law-1", "rule-derived-1", 0.95);
        store.save(edge1).toCompletableFuture().join();

        List<KnowledgeLineageEdge> ancestors = service.traceProvenance("doc-law-1").toCompletableFuture().join();
        assertEquals(1, ancestors.size());
        assertEquals("rule-derived-1", ancestors.get(0).targetId());
    }
}
