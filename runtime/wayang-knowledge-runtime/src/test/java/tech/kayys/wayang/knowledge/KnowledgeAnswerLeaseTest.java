package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.lease.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerLeaseTest {

    @Test
    public void testLeaseStore() {
        InMemoryKnowledgeAnswerResolutionLeaseStore store =
                new InMemoryKnowledgeAnswerResolutionLeaseStore();

        assertNotNull(store);
    }
}
