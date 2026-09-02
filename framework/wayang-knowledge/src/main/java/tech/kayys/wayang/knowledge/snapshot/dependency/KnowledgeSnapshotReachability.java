package tech.kayys.wayang.knowledge.snapshot.dependency;

import java.util.Set;

public record KnowledgeSnapshotReachability(
        Set<String> roots,
        Set<String> reachableSnapshots,
        Set<String> unreachableSnapshots
) {

    public KnowledgeSnapshotReachability {
        roots = Set.copyOf(roots);
        reachableSnapshots = Set.copyOf(reachableSnapshots);
        unreachableSnapshots = Set.copyOf(unreachableSnapshots);
    }
}
