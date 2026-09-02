package tech.kayys.wayang.knowledge.snapshot.dependency;

import java.util.Objects;

public final class DefaultKnowledgeSnapshotDependencyValidator implements KnowledgeSnapshotDependencyValidator {

    @Override
    public void validate(KnowledgeSnapshotDependency dependency) {
        Objects.requireNonNull(dependency, "dependency must not be null");

        if (dependency.snapshotId().value().equals(dependency.targetId())
                && dependency.type() == KnowledgeSnapshotDependencyType.SNAPSHOT) {
            throw new IllegalArgumentException("Snapshot cannot depend on itself");
        }
    }
}
