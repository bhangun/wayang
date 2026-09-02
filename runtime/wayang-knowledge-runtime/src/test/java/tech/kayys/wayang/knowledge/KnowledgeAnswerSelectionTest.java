package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.selection.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerSelectionTest {

    @Test
    public void testDependencyGraphInstantiation() {
        InMemoryKnowledgeAnswerResolutionDependencyGraph graph =
                new InMemoryKnowledgeAnswerResolutionDependencyGraph();

        assertNotNull(graph);
    }
}
