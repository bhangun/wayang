package tech.kayys.wayang.knowledge.integrity;

public record KnowledgeSnapshotIntegrityPolicy(
        boolean verifyKnowledge,
        boolean verifyPolicies,
        boolean verifyRules,
        boolean verifyGovernance,
        boolean verifyRuntime,
        boolean verifyLineage,
        boolean verifyDependencies,
        boolean failOnMissingDependency
) {

    public static KnowledgeSnapshotIntegrityPolicy strict() {
        return new KnowledgeSnapshotIntegrityPolicy(true, true, true, true, true, true, true, true);
    }

    public static KnowledgeSnapshotIntegrityPolicy relaxed() {
        return new KnowledgeSnapshotIntegrityPolicy(true, true, true, true, false, false, true, false);
    }
}
