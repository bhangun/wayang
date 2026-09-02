package tech.kayys.wayang.knowledge.seal;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Map;

public record KnowledgeSnapshotExternalTrustAnchor(
        String anchorId,
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotTrustAnchorType type,
        String externalReference,
        String anchoredDigest,
        Instant anchoredAt,
        String providerId,
        String providerVersion,
        Map<String, String> metadata
) {

    public KnowledgeSnapshotExternalTrustAnchor {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
