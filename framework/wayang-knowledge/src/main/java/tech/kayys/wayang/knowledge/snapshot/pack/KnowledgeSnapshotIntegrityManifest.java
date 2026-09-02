package tech.kayys.wayang.knowledge.snapshot.pack;

import tech.kayys.wayang.knowledge.integrity.KnowledgeSnapshotIntegrityMismatch;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record KnowledgeSnapshotIntegrityManifest(
        String status,
        String verifierId,
        String verifierVersion,
        Instant verifiedAt,
        List<KnowledgeSnapshotIntegrityMismatch> mismatches,
        String aggregateFingerprint,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotIntegrityManifest {
        mismatches = mismatches == null ? List.of() : List.copyOf(mismatches);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
