package tech.kayys.wayang.knowledge.snapshot.pack;

import java.util.List;
import java.util.Map;

public record KnowledgeSnapshotMerkleManifest(
        String algorithm,
        String rootHash,
        int leafCount,
        List<String> requiredLeafIds,
        Map<String, String> metadata
) {
    public KnowledgeSnapshotMerkleManifest {
        requiredLeafIds = requiredLeafIds == null ? List.of() : List.copyOf(requiredLeafIds);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
