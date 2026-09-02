package tech.kayys.wayang.knowledge;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.exchange.lease.*;
import tech.kayys.wayang.knowledge.exchange.recovery.*;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeAnswerRecoveryTest {

    @Test
    public void testRecoveryCoordinatorInstantiation() {
        var leaseStore = new InMemoryKnowledgeAnswerResolutionLeaseStore();
        var heartbeatStore = new InMemoryKnowledgeAnswerResolutionHeartbeatStore();
        var evaluator = new DefaultKnowledgeAnswerResolutionFreshnessEvaluator(heartbeatStore);
        var leaseService = new DefaultKnowledgeAnswerResolutionLeaseService(leaseStore, evaluator);
        var freshnessService = new DefaultKnowledgeAnswerResolutionFreshnessService(leaseService);

        KnowledgeAnswerResolutionDependencyValidator depValidator = (k, t, w, p) -> true;
        KnowledgeAnswerResolutionSnapshotValidator snapValidator = (c, k, t, w, p) -> true;
        KnowledgeAnswerResolutionAttestationValidator attValidator = (c, k) -> true;

        DefaultKnowledgeAnswerResolutionRevalidationEngine engine =
                new DefaultKnowledgeAnswerResolutionRevalidationEngine(
                        freshnessService, depValidator, snapValidator, attValidator, leaseStore
                );

        DefaultKnowledgeAnswerResolutionRecoveryCoordinator coordinator =
                new DefaultKnowledgeAnswerResolutionRecoveryCoordinator(engine);

        assertNotNull(coordinator);
    }
}
