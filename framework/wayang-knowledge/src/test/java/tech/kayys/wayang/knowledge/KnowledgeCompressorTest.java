package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeCompressorTest {

    @Test
    void testExtractiveCompressionPreservesProvenance() {
        KnowledgeProvenance prov = KnowledgeProvenance.of("file:///statute.pdf");
        String longText = "A".repeat(500);
        KnowledgeItem item = new KnowledgeItem("item-long", "src-1", "statute", "Article 1", longText, null, prov, KnowledgeAuthority.informational(), KnowledgeValidity.active());
        KnowledgeEvidence ev = new KnowledgeEvidence(item, 0.88, "hybrid", java.util.Map.of());

        KnowledgeCompressor compressor = new ExtractiveKnowledgeCompressor(100);
        List<KnowledgeEvidence> compressed = compressor.compress(List.of(ev), KnowledgeBudget.slm());

        assertEquals(1, compressed.size());
        assertTrue(compressed.get(0).item().content().length() <= 105);
        assertEquals("file:///statute.pdf", compressed.get(0).item().provenance().sourceUri());
    }
}
