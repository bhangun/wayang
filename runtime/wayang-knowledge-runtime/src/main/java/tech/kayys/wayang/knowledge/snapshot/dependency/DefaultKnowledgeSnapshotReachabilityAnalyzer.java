package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public final class DefaultKnowledgeSnapshotReachabilityAnalyzer implements KnowledgeSnapshotReachabilityAnalyzer {

    private final List<KnowledgeSnapshotRetentionRoot> roots;
    private final KnowledgeSnapshotDependencyGraph graph;
    private final List<KnowledgeDecisionSnapshot> snapshots;

    public DefaultKnowledgeSnapshotReachabilityAnalyzer(
            List<KnowledgeSnapshotRetentionRoot> roots,
            KnowledgeSnapshotDependencyGraph graph,
            List<KnowledgeDecisionSnapshot> snapshots) {

        this.roots = List.copyOf(roots);
        this.graph = Objects.requireNonNull(graph);
        this.snapshots = List.copyOf(snapshots);
    }

    @Override
    public KnowledgeSnapshotReachability analyze(Instant now) {
        Set<String> rootIds = new HashSet<>();
        Set<String> reachable = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();

        for (KnowledgeSnapshotRetentionRoot root : roots) {
            if (!root.activeAt(now)) {
                continue;
            }
            rootIds.add(root.snapshotId().value());
            queue.add(root.snapshotId().value());
        }

        while (!queue.isEmpty()) {
            String snapshotId = queue.removeFirst();
            if (!reachable.add(snapshotId)) {
                continue;
            }

            KnowledgeSnapshotId id = new KnowledgeSnapshotId(snapshotId, "SHA-256", java.time.Instant.now());
            for (KnowledgeSnapshotDependency dep : graph.dependencies(id)) {
                if (dep.type() == KnowledgeSnapshotDependencyType.SNAPSHOT) {
                    queue.add(dep.targetId());
                }
            }
        }

        Set<String> allSnapshots = snapshots.stream()
                .map(s -> s.snapshotId().value())
                .collect(Collectors.toSet());

        Set<String> unreachable = new HashSet<>(allSnapshots);
        unreachable.removeAll(reachable);

        return new KnowledgeSnapshotReachability(rootIds, reachable, unreachable);
    }
}
