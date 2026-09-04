package tech.kayys.wayang.knowledge.snapshot.cache;

import java.util.Arrays;
import java.util.Map;

public record KnowledgeAnswerResolutionSharedStateBlock(
        KnowledgeAnswerResolutionStateBlockId id,
        byte[] data,
        long sizeBytes,
        long createdAtEpochMillis,
        Map<String, String> metadata
) {
    public KnowledgeAnswerResolutionSharedStateBlock {
        data = data == null ? new byte[0] : Arrays.copyOf(data, data.length);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public byte[] data() {
        return Arrays.copyOf(data, data.length);
    }
}
