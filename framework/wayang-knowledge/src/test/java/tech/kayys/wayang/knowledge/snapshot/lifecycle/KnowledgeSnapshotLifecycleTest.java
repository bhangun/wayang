package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.decision.*;
import tech.kayys.wayang.knowledge.snapshot.*;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSnapshotLifecycleTest {

    @Test
    void testLifecycleReferencesAndHolds() {
        InMemoryKnowledgeSnapshotRegistryStore store = new InMemoryKnowledgeSnapshotRegistryStore();
        KnowledgeSnapshotRetentionPolicy policy = KnowledgeSnapshotRetentionPolicy.defaults();
        DefaultKnowledgeSnapshotRegistry registry = new DefaultKnowledgeSnapshotRegistry(store, policy);
        DefaultKnowledgeSnapshotDeletionService deletionService = new DefaultKnowledgeSnapshotDeletionService(registry, store);

        KnowledgeSnapshotId snapId = KnowledgeSnapshotId.of("snap-hash-123");
        KnowledgeDecisionSnapshot snapshot = new KnowledgeDecisionSnapshot(
                snapId,
                "exec-1", "trace-1", "agent-1", "op", "query",
                Instant.now(), List.of(), null, null, null, null, "fp", Instant.now(), java.util.Map.of()
        );

        registry.register(snapshot);

        // Initially no refs, can be deleted
        KnowledgeSnapshotLifecycleDecision decision = registry.evaluate(snapId, Instant.now());
        assertEquals(KnowledgeSnapshotDeletionDecision.DELETE, decision.decision());

        // Add active reference
        KnowledgeSnapshotReference ref = new KnowledgeSnapshotReference(
                "ref-1", snapId, KnowledgeSnapshotReferenceType.DECISION_TRACE,
                "owner-1", "tenant-1", Instant.now(), Instant.now().plusSeconds(3600), java.util.Map.of()
        );
        registry.addReference(ref);

        decision = registry.evaluate(snapId, Instant.now());
        assertEquals(KnowledgeSnapshotDeletionDecision.RETAIN, decision.decision());
        assertThrows(IllegalStateException.class, () -> deletionService.delete(snapId));

        // Add legal hold
        KnowledgeSnapshotHold hold = new KnowledgeSnapshotHold(
                "hold-1", snapId, KnowledgeSnapshotRetentionClass.LEGAL_HOLD,
                "tenant-1", "Regulatory audit", Instant.now(), null, java.util.Map.of()
        );
        registry.addHold(hold);

        decision = registry.evaluate(snapId, Instant.now());
        assertEquals(KnowledgeSnapshotDeletionDecision.BLOCKED, decision.decision());

        // Remove hold and reference
        registry.removeHold("hold-1");
        registry.removeReference("ref-1");

        decision = registry.evaluate(snapId, Instant.now());
        assertEquals(KnowledgeSnapshotDeletionDecision.DELETE, decision.decision());
        assertDoesNotThrow(() -> deletionService.delete(snapId));
    }
}
