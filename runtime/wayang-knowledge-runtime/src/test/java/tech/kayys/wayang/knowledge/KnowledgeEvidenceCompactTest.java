package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.compact.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceCompactTest {

    @Test
    public void testArtifactDeduplicationService() {
        DefaultKnowledgeAnswerArtifactSimilarityService similarity =
                new DefaultKnowledgeAnswerArtifactSimilarityService();

        DefaultKnowledgeAnswerArtifactDeduplicationService deduplication =
                new DefaultKnowledgeAnswerArtifactDeduplicationService(similarity);

        assertNotNull(deduplication);
    }
}
