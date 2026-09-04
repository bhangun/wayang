package tech.kayys.wayang.knowledge.snapshot.delta;

import java.util.Arrays;
import java.util.Map;

public record KnowledgeAnswerResolutionStateDeltaOperation(
        Type type,
        String key,
        String previousFingerprint,
        String valueFingerprint,
        byte[] value,
        Map<String, String> metadata
) {
    public enum Type {
        PUT,
        REMOVE,
        REPLACE
    }

    public KnowledgeAnswerResolutionStateDeltaOperation {
        value = value == null ? null : Arrays.copyOf(value, value.length);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public byte[] value() {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }
}
