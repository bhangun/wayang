package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.List;
import java.util.Map;

public record KnowledgeAnswerResolutionStateBlockManifest(
        String snapshotId,
        String tenantId,
        String epochId,
        long lastAppliedIndex,
        String stateFingerprint,
        String merkleRoot,
        List<String> blockIds,
        long totalBytes,
        Map<String, String> metadata
) {
    public KnowledgeAnswerResolutionStateBlockManifest {
        blockIds = blockIds == null ? List.of() : List.copyOf(blockIds);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
