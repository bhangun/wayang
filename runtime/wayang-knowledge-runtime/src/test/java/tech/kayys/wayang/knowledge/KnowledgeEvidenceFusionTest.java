package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.fusion.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceFusionTest {

    @Test
    public void testAuthorityAndTrustScorers() {
        DefaultKnowledgeEvidenceAuthorityScorer authScorer =
                new DefaultKnowledgeEvidenceAuthorityScorer();

        DefaultKnowledgeEvidenceTrustScorer trustScorer =
                new DefaultKnowledgeEvidenceTrustScorer();

        KnowledgeEvidenceReference ref1 = new KnowledgeEvidenceReference(
                "know-1", "v1", "frag-1", "Official specification excerpt", 0.95, 0.9, 0.99, Map.of()
        );

        double authScore = authScorer.score(ref1);
        double trustScore = trustScorer.score(ref1);

        assertTrue(authScore > 0.0);
        assertTrue(trustScore > 0.0);
    }

    @Test
    public void testConflictDetectionAndResolution() {
        DefaultKnowledgeEvidenceSimilarityService similarityService =
                new DefaultKnowledgeEvidenceSimilarityService();

        DefaultKnowledgeEvidenceConflictDetector conflictDetector =
                new DefaultKnowledgeEvidenceConflictDetector(similarityService);

        KnowledgeEvidenceReference refA = new KnowledgeEvidenceReference(
                "k-1", "v1", "frag-1", "The state transition is synchronous", 0.8, 0.9, 0.9, Map.of()
        );
        KnowledgeEvidenceReference refB = new KnowledgeEvidenceReference(
                "k-1", "v2", "frag-1", "The state transition is completely asynchronous", 0.85, 0.7, 0.8, Map.of()
        );

        KnowledgeEvidenceFusionCandidate candA = new KnowledgeEvidenceFusionCandidate(
                refA, "rt-1", 0.8, 0.8, 0.9, 0.9, 0.8, 0.85, Map.of()
        );
        KnowledgeEvidenceFusionCandidate candB = new KnowledgeEvidenceFusionCandidate(
                refB, "rt-2", 0.85, 0.85, 0.7, 0.8, 0.9, 0.82, Map.of()
        );

        var conflicts = conflictDetector.detect(List.of(candA, candB));
        assertNotNull(conflicts);

        DefaultKnowledgeEvidenceConflictResolutionPolicy resolutionPolicy =
                new DefaultKnowledgeEvidenceConflictResolutionPolicy();

        if (!conflicts.isEmpty()) {
            var decision = resolutionPolicy.resolve(conflicts.get(0));
            assertNotNull(decision);
        }
    }
}
