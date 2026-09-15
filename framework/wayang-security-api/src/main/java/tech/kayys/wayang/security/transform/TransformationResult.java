package tech.kayys.wayang.security.transform;

import java.util.Map;

/**
 * Result of a data transformation operation.
 */
public record TransformationResult(
        Object data,
        boolean modified,
        Map<String, Object> attributes
) {

    public TransformationResult {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static TransformationResult unchanged(Object data) {
        return new TransformationResult(data, false, Map.of());
    }

    public static TransformationResult modified(Object data) {
        return new TransformationResult(data, true, Map.of());
    }
}
