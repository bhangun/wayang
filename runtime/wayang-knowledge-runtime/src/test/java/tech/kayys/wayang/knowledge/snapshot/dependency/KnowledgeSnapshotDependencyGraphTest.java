package tech.kayys.wayang.knowledge.snapshot.dependency;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.knowledge.snapshot.*;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.KnowledgeSnapshotDeletionDecision;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class KnowledgeSnapshotDependencyGraphTest {

    @Test
    void testReachabilityAndCascadingGC() {
        InMemoryKnowledgeSnapshotDependencyGraph graph = new InMemoryKnowledgeSnapshotDependencyGraph();

        KnowledgeSnapshotId snapA = KnowledgeSnapshotId.of("snap-A");
        KnowledgeSnapshotId snapB = KnowledgeSnapshotId.of("snap-B");
        KnowledgeSnapshotId snapC = KnowledgeSnapshotId.of("snap-C");

        // A depends on B
        graph.addDependency(new KnowledgeSnapshotDependency(
                "dep-1", snapA, KnowledgeSnapshotDependencyType.SNAPSHOT,
                "snap-B", "v1", true, true, Instant.now(), java.util.Map.of()
        ));

        // B depends on C
        graph.addDependency(new KnowledgeSnapshotDependency(
                "dep-2", snapB, KnowledgeSnapshotDependencyType.SNAPSHOT,
                "snap-C", "v1", true, true, Instant.now(), java.util.Map.of()
        ));

        // Only A is a root
        KnowledgeSnapshotRetentionRoot root = new KnowledgeSnapshotRetentionRoot(
                "root-1", snapA, KnowledgeSnapshotRetentionRootType.ACTIVE_EXECUTION,
                "tenant-1", "agent-1", Instant.now(), Instant.now().plusSeconds(600), java.util.Map.of()
        );

        List<KnowledgeDecisionSnapshot> snapshots = List.of(
                createDummySnapshot(snapA),
                createDummySnapshot(snapB),
                createDummySnapshot(snapC),
                createDummySnapshot(KnowledgeSnapshotId.of("snap-orphan"))
        );

        DefaultKnowledgeSnapshotReachabilityAnalyzer analyzer = new DefaultKnowledgeSnapshotReachabilityAnalyzer(
                List.of(root), graph, snapshots
        );

        KnowledgeSnapshotReachability reachability = analyzer.analyze(Instant.now());
        assertTrue(reachability.reachableSnapshots().contains("snap-A"));
        assertTrue(reachability.reachableSnapshots().contains("snap-B"));
        assertTrue(reachability.reachableSnapshots().contains("snap-C"));
        assertTrue(reachability.unreachableSnapshots().contains("snap-orphan"));

        DefaultKnowledgeSnapshotCascadingGarbageCollector gc = new DefaultKnowledgeSnapshotCascadingGarbageCollector(
                analyzer, graph, snapshots
        );

        List<KnowledgeSnapshotCascadingRetentionDecision> decisions = gc.collect(Instant.now());
        assertEquals(4, decisions.size());

        for (var d : decisions) {
            if (d.snapshotId().value().equals("snap-orphan")) {
                assertEquals(KnowledgeSnapshotDeletionDecision.DELETE, d.decision());
            } else {
                assertEquals(KnowledgeSnapshotDeletionDecision.RETAIN, d.decision());
            }
        }
    }

    private KnowledgeDecisionSnapshot createDummySnapshot(KnowledgeSnapshotId id) {
        return new KnowledgeDecisionSnapshot(
                id, "exec-1", "trace-1", "agent-1", "op", "query",
                Instant.now(), List.of(), null, null, null, null, "fp", Instant.now(), java.util.Map.of()
        );
    }
}
