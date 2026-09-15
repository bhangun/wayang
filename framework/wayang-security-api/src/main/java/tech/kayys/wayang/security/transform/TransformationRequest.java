package tech.kayys.wayang.security.transform;

import java.util.Map;
import java.util.Objects;

/**
 * Request to transform or sanitize execution data according to policy.
 */
public record TransformationRequest(
        Object data,
        TransformationOperation operation,
        Map<String, Object> parameters,
        Map<String, Object> attributes
) {

    public TransformationRequest {
        Objects.requireNonNull(operation, "operation");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static TransformationRequest of(Object data, TransformationOperation operation, Map<String, Object> parameters) {
        return new TransformationRequest(data, operation, parameters, Map.of());
    }
}
