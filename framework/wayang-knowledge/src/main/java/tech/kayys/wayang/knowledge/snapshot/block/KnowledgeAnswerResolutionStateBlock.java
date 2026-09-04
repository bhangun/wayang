package tech.kayys.wayang.knowledge.snapshot.block;

import java.util.Arrays;
import java.util.Map;

public record KnowledgeAnswerResolutionStateBlock(
        String blockId,
        String algorithm,
        String fingerprint,
        byte[] data,
        long sizeBytes,
        Map<String, String> metadata
) {
    public KnowledgeAnswerResolutionStateBlock {
        data = data == null ? new byte[0] : Arrays.copyOf(data, data.length);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public byte[] data() {
        return Arrays.copyOf(data, data.length);
    }
}
