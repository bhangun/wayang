package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeSnapshotDependencyGraph implements KnowledgeSnapshotDependencyGraph {

    private final Map<String, KnowledgeSnapshotDependency> dependencies = new ConcurrentHashMap<>();

    @Override
    public void addDependency(KnowledgeSnapshotDependency dependency) {
        dependencies.put(dependency.dependencyId(), dependency);
    }

    @Override
    public void removeDependency(String dependencyId) {
        dependencies.remove(dependencyId);
    }

    @Override
    public List<KnowledgeSnapshotDependency> dependencies(KnowledgeSnapshotId snapshotId) {
        return dependencies.values().stream()
                .filter(d -> d.snapshotId().equals(snapshotId))
                .toList();
    }

    @Override
    public List<KnowledgeSnapshotDependency> dependents(String targetId) {
        return dependencies.values().stream()
                .filter(d -> d.targetId().equals(targetId))
                .toList();
    }

    @Override
    public List<KnowledgeSnapshotDependency> transitiveDependencies(KnowledgeSnapshotId snapshotId) {
        List<KnowledgeSnapshotDependency> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        ArrayDeque<KnowledgeSnapshotId> queue = new ArrayDeque<>();
        queue.add(snapshotId);

        while (!queue.isEmpty()) {
            KnowledgeSnapshotId current = queue.removeFirst();
            if (!visited.add(current.value())) {
                continue;
            }

            List<KnowledgeSnapshotDependency> direct = dependencies(current);
            result.addAll(direct);

            for (KnowledgeSnapshotDependency dep : direct) {
                if (dep.type() == KnowledgeSnapshotDependencyType.SNAPSHOT) {
                    queue.add(new KnowledgeSnapshotId(dep.targetId(), "SHA-256", java.time.Instant.now()));
                }
            }
        }

        return List.copyOf(result);
    }
}
