package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Map;

public record KnowledgeAttestation(
        String attestationId,
        KnowledgeSnapshotId snapshotId,
        String statement,
        String attesterId,
        String attesterType,
        String signature,
        Instant attestedAt,
        Map<String, Object> metadata
) {

    public KnowledgeAttestation {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
