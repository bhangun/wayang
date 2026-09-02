package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.factuality.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceFactualityTest {

    @Test
    public void testArtifactVerifierInstantiation() {
        DefaultKnowledgeVerifiedAnswerArtifactVerifier verifier =
                new DefaultKnowledgeVerifiedAnswerArtifactVerifier();

        assertNotNull(verifier);
    }
}
