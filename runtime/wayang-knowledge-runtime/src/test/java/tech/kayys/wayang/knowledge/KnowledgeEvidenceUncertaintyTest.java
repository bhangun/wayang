package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.uncertainty.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeEvidenceUncertaintyTest {

    @Test
    public void testArtifactIndexInstantiation() {
        InMemoryKnowledgeAnswerArtifactIndex index =
                new InMemoryKnowledgeAnswerArtifactIndex();

        assertNotNull(index);
    }
}
