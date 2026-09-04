package tech.kayys.wayang.knowledge.snapshot.packing;

import java.util.Arrays;
import java.util.Map;

public record KnowledgeAnswerResolutionLogicalStateBlock(
        String logicalId,
        byte[] data,
        Map<String, String> metadata
) {
    public KnowledgeAnswerResolutionLogicalStateBlock {
        data = data == null ? new byte[0] : Arrays.copyOf(data, data.length);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public long sizeBytes() {
        return data == null ? 0 : data.length;
    }

    @Override
    public byte[] data() {
        return Arrays.copyOf(data, data.length);
    }
}
