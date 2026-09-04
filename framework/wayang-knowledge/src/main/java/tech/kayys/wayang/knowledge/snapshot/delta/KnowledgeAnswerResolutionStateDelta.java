package tech.kayys.wayang.knowledge.snapshot.delta;

import java.util.List;
import java.util.Map;

public record KnowledgeAnswerResolutionStateDelta(
        String deltaId,
        String sourceSnapshotId,
        String targetSnapshotId,
        String tenantId,
        String sourceEpochId,
        String targetEpochId,
        long sourceIndex,
        long targetIndex,
        String sourceStateFingerprint,
        String targetStateFingerprint,
        String deltaFingerprint,
        List<KnowledgeAnswerResolutionStateDeltaOperation> operations,
        long estimatedFullSnapshotBytes,
        long estimatedDeltaBytes,
        Map<String, String> metadata
) {
    public KnowledgeAnswerResolutionStateDelta {
        operations = operations == null ? List.of() : List.copyOf(operations);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
