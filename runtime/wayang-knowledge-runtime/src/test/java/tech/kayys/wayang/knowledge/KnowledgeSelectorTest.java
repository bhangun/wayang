package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSelectorTest {

    @Test
    void testBudgetAndQuotaEnforcement() {
        KnowledgeProvenance prov = KnowledgeProvenance.of("file:///kb.md");
        KnowledgeItem itemA1 = new KnowledgeItem("a1", "source-A", "rule", "A1", "Content A1", null, prov, KnowledgeAuthority.authoritative("policy", "legal", 1), KnowledgeValidity.active());
        KnowledgeItem itemA2 = new KnowledgeItem("a2", "source-A", "rule", "A2", "Content A2", null, prov, KnowledgeAuthority.informational(), KnowledgeValidity.active());
        KnowledgeItem itemB1 = new KnowledgeItem("b1", "source-B", "rule", "B1", "Content B1", null, prov, KnowledgeAuthority.informational(), KnowledgeValidity.active());

        KnowledgeEvidence evA1 = new KnowledgeEvidence(itemA1, 0.9, "v", java.util.Map.of());
        KnowledgeEvidence evA2 = new KnowledgeEvidence(itemA2, 0.85, "v", java.util.Map.of());
        KnowledgeEvidence evB1 = new KnowledgeEvidence(itemB1, 0.8, "v", java.util.Map.of());

        KnowledgeBudget budget = new KnowledgeBudget(2, 500, 1000, 1, 2, false, true, 0.1);
        KnowledgeSelector selector = new DefaultKnowledgeSelector();

        KnowledgeSelectionResult res = selector.select(List.of(evA1, evA2, evB1), budget, KnowledgeContext.empty());

        assertEquals(2, res.selected().size());
        assertEquals("a1", res.selected().get(0).item().id());
        assertEquals("b1", res.selected().get(1).item().id(), "Source A quota (max 1) must force selection of Source B");
        assertEquals(1, res.excluded().size());
    }
}
