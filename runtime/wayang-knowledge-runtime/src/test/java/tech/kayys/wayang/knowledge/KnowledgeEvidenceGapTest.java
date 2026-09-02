package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.gap.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceGapTest {

    @Test
    public void testGapFillingAndVerification() {
        KnowledgeEvidenceClaim claim = new KnowledgeEvidenceClaim(
                "claim-1", "The encryption standard is AES-256", KnowledgeEvidenceClaimType.FACT,
                true, List.of("encryption"), Map.of()
        );

        KnowledgeEvidenceClaimSupport support = new KnowledgeEvidenceClaimSupport(
                "claim-1", "ev-1", 0.95, true, true, true, "Strong match", Map.of()
        );

        KnowledgeEvidenceClaimGraph graph = new KnowledgeEvidenceClaimGraph(
                "graph-1", "Answer statement", List.of(claim), List.of(support), List.of(), Map.of()
        );

        assertNotNull(graph);
        assertEquals(1, graph.claims().size());

        DefaultKnowledgeEvidenceClaimVerifier verifier =
                new DefaultKnowledgeEvidenceClaimVerifier();

        KnowledgeEvidenceClaimVerification verification = verifier.verify(claim, graph);
        assertNotNull(verification);
        assertEquals(KnowledgeEvidenceClaimStatus.SUPPORTED, verification.status());
    }
}
