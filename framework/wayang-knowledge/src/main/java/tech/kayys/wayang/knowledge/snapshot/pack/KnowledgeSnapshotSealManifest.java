package tech.kayys.wayang.knowledge.snapshot.pack;

import java.time.Instant;
import java.util.Map;

public record KnowledgeSnapshotSealManifest(
        String sealId,
        String algorithm,
        String anchorType,
        String keyId,
        String keyVersion,
        String signature,
        Instant createdAt,
        Instant expiresAt,
        String status,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotSealManifest {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
