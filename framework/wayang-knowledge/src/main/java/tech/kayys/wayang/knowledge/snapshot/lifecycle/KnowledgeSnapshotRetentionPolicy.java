package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import java.time.Duration;
import java.util.Map;

public record KnowledgeSnapshotRetentionPolicy(
        Map<KnowledgeSnapshotRetentionClass, Duration> retentionPeriods,
        boolean allowAutomaticDeletion,
        boolean archiveBeforeDelete,
        boolean requireNoReferences,
        boolean requireNoHolds
) {

    public KnowledgeSnapshotRetentionPolicy {
        retentionPeriods = retentionPeriods == null ? Map.of() : Map.copyOf(retentionPeriods);
    }

    public Duration retentionFor(KnowledgeSnapshotRetentionClass retentionClass) {
        return retentionPeriods.get(retentionClass);
    }

    public static KnowledgeSnapshotRetentionPolicy defaults() {
        return new KnowledgeSnapshotRetentionPolicy(
                Map.of(
                        KnowledgeSnapshotRetentionClass.TRANSIENT, Duration.ofHours(1),
                        KnowledgeSnapshotRetentionClass.STANDARD, Duration.ofDays(30),
                        KnowledgeSnapshotRetentionClass.AUDIT, Duration.ofDays(365),
                        KnowledgeSnapshotRetentionClass.ARCHIVAL, Duration.ofDays(3650)
                ),
                true,
                true,
                true,
                true
        );
    }
}
