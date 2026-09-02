package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.attestation.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerAttestationTest {

    @Test
    public void testParticipantRegistry() {
        InMemoryKnowledgeAnswerResolutionConsensusParticipantRegistry registry =
                new InMemoryKnowledgeAnswerResolutionConsensusParticipantRegistry();

        assertNotNull(registry);
    }
}
