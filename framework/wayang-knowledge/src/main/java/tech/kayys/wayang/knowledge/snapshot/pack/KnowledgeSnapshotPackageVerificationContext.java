package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.Map;

public record KnowledgeSnapshotPackageVerificationContext(
        boolean requireIntegrity,
        boolean requireSeal,
        boolean requireDependencies,
        boolean requireGovernanceMatch,
        boolean requirePolicyMatch,
        boolean requireRuleMatch,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotPackageVerificationContext {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeSnapshotPackageVerificationContext strict() {
        return new KnowledgeSnapshotPackageVerificationContext(
                true, true, true, true, true, true, Map.of()
        );
    }

    public static KnowledgeSnapshotPackageVerificationContext relaxed() {
        return new KnowledgeSnapshotPackageVerificationContext(
                false, false, false, false, false, false, Map.of()
        );
    }
}
