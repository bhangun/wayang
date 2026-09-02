package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.KnowledgeSnapshotDeletionDecision;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultKnowledgeSnapshotCascadingGarbageCollector implements KnowledgeSnapshotCascadingGarbageCollector {

    private final KnowledgeSnapshotReachabilityAnalyzer analyzer;
    private final KnowledgeSnapshotDependencyGraph graph;
    private final List<KnowledgeDecisionSnapshot> snapshots;

    public DefaultKnowledgeSnapshotCascadingGarbageCollector(
            KnowledgeSnapshotReachabilityAnalyzer analyzer,
            KnowledgeSnapshotDependencyGraph graph,
            List<KnowledgeDecisionSnapshot> snapshots) {

        this.analyzer = Objects.requireNonNull(analyzer);
        this.graph = Objects.requireNonNull(graph);
        this.snapshots = List.copyOf(snapshots);
    }

    @Override
    public List<KnowledgeSnapshotCascadingRetentionDecision> collect(Instant now) {
        KnowledgeSnapshotReachability reachability = analyzer.analyze(now);
        List<KnowledgeSnapshotCascadingRetentionDecision> result = new ArrayList<>();

        for (KnowledgeDecisionSnapshot snapshot : snapshots) {
            String id = snapshot.snapshotId().value();
            if (reachability.reachableSnapshots().contains(id)) {
                result.add(new KnowledgeSnapshotCascadingRetentionDecision(
                        snapshot.snapshotId(),
                        KnowledgeSnapshotDeletionDecision.RETAIN,
                        reachability.reachableSnapshots(),
                        graph.dependencies(snapshot.snapshotId()),
                        List.of("Snapshot reachable from retention root")
                ));
            } else {
                result.add(new KnowledgeSnapshotCascadingRetentionDecision(
                        snapshot.snapshotId(),
                        KnowledgeSnapshotDeletionDecision.DELETE,
                        reachability.reachableSnapshots(),
                        List.of(),
                        List.of("Snapshot is unreachable from retention roots")
                ));
            }
        }

        return result;
    }
}
