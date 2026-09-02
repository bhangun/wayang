package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.attribution.*;
import tech.kayys.wayang.knowledge.exchange.fusion.*;
import tech.kayys.wayang.knowledge.exchange.gap.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceAttributionTest {

    @Test
    public void testAttributionCitationCompilation() {
        DefaultKnowledgeVerifiedEvidenceCitationCompiler compiler =
                new DefaultKnowledgeVerifiedEvidenceCitationCompiler();

        KnowledgeEvidenceReference ref1 = new KnowledgeEvidenceReference(
                "know-1", "v1", "frag-1", "Excerpt on authentication", 0.95, 0.9, 0.99,
                Map.of("artifactId", "art-1", "provenanceId", "prov-1")
        );

        KnowledgeEvidenceFusionCandidate cand1 = new KnowledgeEvidenceFusionCandidate(
                ref1, "rt-1", 0.9, 0.9, 0.95, 0.99, 0.85, 0.92, Map.of()
        );

        KnowledgeEvidenceFusionResult fusionResult = new KnowledgeEvidenceFusionResult(
                List.of(cand1), List.of(cand1), List.of(), List.of(), true, false, Map.of()
        );

        KnowledgeEvidenceClaim claim = new KnowledgeEvidenceClaim(
                "claim-1", "Statement on authentication", KnowledgeEvidenceClaimType.FACT,
                true, List.of("authentication"), Map.of()
        );

        KnowledgeEvidenceClaimVerification claimVer = new KnowledgeEvidenceClaimVerification(
                "claim-1", KnowledgeEvidenceClaimStatus.SUPPORTED, 0.95, List.of("know-1"), List.of(), "Verified by ref1", Map.of()
        );

        KnowledgeEvidenceAnswerVerification answerVer = new KnowledgeEvidenceAnswerVerification(
                "ver-1", new KnowledgeEvidenceClaimGraph("graph-1", "Statement", List.of(claim), List.of(), List.of(), Map.of()),
                List.of(claimVer), KnowledgeEvidenceClaimStatus.SUPPORTED, 0.95, true, List.of(), Map.of()
        );

        var citations = compiler.compile(answerVer, fusionResult);
        assertNotNull(citations);
        assertFalse(citations.isEmpty());
        assertEquals("know-1", citations.get(0).knowledgeId());
        assertEquals("art-1", citations.get(0).artifactId());
    }
}
