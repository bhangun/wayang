package tech.kayys.wayang.knowledge.snapshot;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.decision.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSnapshotTest {

    @Test
    void testSnapshotCaptureAndImmutability() {
        KnowledgeSnapshotCanonicalizer canonicalizer = new DefaultKnowledgeSnapshotCanonicalizer();
        KnowledgeDecisionSnapshotFactory factory = new KnowledgeDecisionSnapshotFactory(canonicalizer);
        InMemoryKnowledgeDecisionSnapshotStore store = new InMemoryKnowledgeDecisionSnapshotStore();
        DefaultKnowledgeDecisionSnapshotService service = new DefaultKnowledgeDecisionSnapshotService(factory, store);

        KnowledgeDecisionTrace trace = new KnowledgeDecisionRecorder("trace-snap-1")
                .executionId("exec-100")
                .agentId("agent-gamma")
                .operation("INJECT")
                .outcome(KnowledgeDecisionOutcome.allowed("OK", "Injected"))
                .build();

        KnowledgeSnapshotCaptureContext context = new KnowledgeSnapshotCaptureContext(
                List.of(new KnowledgeSnapshotEntry("k-1", "v1", "fp-1", "prov-1", "auth-1", "trust-1", "lin-1", java.util.Map.of())),
                new KnowledgePolicySnapshot(List.of(new KnowledgeVersionReference("p-1", "v1", "policy", "fp-p1", java.util.Map.of())), "p-agg", java.util.Map.of()),
                new KnowledgeRuleSnapshot(List.of(new KnowledgeVersionReference("r-1", "v1", "rule", "fp-r1", java.util.Map.of())), "r-agg", java.util.Map.of()),
                new KnowledgeGovernanceSnapshot("tenant-A", "ws-A", "proj-A", "user-A", "2026-09-02T00:00:00Z", "scope-fp", "gov-fp", java.util.Map.of()),
                new KnowledgeRuntimeSnapshot("1.0", "1.0", "1.0", "1.0", "1.0", "openai", "gpt-4", "latest", java.util.Map.of())
        );

        KnowledgeDecisionSnapshot snapshot = service.capture(trace, context);
        assertNotNull(snapshot.snapshotId());
        assertFalse(snapshot.snapshotId().value().isBlank());

        var retrieved = service.get(snapshot.snapshotId().value());
        assertTrue(retrieved.isPresent());
        assertEquals("exec-100", retrieved.get().executionId());
        assertEquals("trace-snap-1", retrieved.get().traceId());

        // Immutability: re-saving different content with same ID throws exception
        KnowledgeDecisionSnapshot mutated = new KnowledgeDecisionSnapshot(
                snapshot.snapshotId(),
                "different-exec",
                trace.id(),
                trace.agentId(),
                trace.operation(),
                trace.query(),
                trace.createdAt(),
                List.of(),
                snapshot.policies(),
                snapshot.rules(),
                snapshot.governance(),
                snapshot.runtime(),
                snapshot.aggregateFingerprint(),
                snapshot.createdAt(),
                snapshot.metadata()
        );
        assertThrows(IllegalStateException.class, () -> store.save(mutated));
    }
}
