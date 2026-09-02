package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.proof.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerProofTest {

    @Test
    public void testAttestationVerifierResolver() {
        InMemoryKnowledgeAnswerResolutionAttestationVerifierResolver resolver =
                new InMemoryKnowledgeAnswerResolutionAttestationVerifierResolver();

        assertNotNull(resolver);
    }
}
