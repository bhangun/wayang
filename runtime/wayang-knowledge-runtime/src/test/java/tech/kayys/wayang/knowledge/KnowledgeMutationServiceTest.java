package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeMutationServiceTest {

    @Test
    void testAddAndSupersedeMutations() {
        VersionedKnowledgeStore store = new InMemoryVersionedKnowledgeStore();
        KnowledgeMutationService service = new DefaultKnowledgeMutationService(store);
        KnowledgeProvenance prov = KnowledgeProvenance.of("human://auditor");

        KnowledgeItem itemOld = new KnowledgeItem("tax-rule-2025", "src-human", "tax", "Tax 2025", "Rate is 10%", null, prov, KnowledgeAuthority.informational(), KnowledgeValidity.active());
        KnowledgeMutationResult res1 = service.apply(KnowledgeMutationRequest.add(itemOld, "alice", "New year tax")).toCompletableFuture().join();
        assertTrue(res1.success());

        KnowledgeItem itemNew = new KnowledgeItem("tax-rule-2026", "src-human", "tax", "Tax 2026", "Rate is 12%", null, prov, KnowledgeAuthority.informational(), KnowledgeValidity.active());
        KnowledgeMutationResult res2 = service.apply(KnowledgeMutationRequest.supersede("tax-rule-2025", itemNew, "bob", "Updated rate")).toCompletableFuture().join();
        assertTrue(res2.success());
        assertEquals("tax-rule-2025", res2.supersededItemId());
    }
}
