package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a failure in an agent invocation.
 */
public record AgentError(
        String code,
        String message,
        Throwable cause,
        Map<String, Object> metadata
) {
    public AgentError {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public Optional<Throwable> findCause() {
        return Optional.ofNullable(cause);
    }

    public static AgentError of(String code, String message) {
        return new AgentError(code, message, null, Map.of());
    }

    public static AgentError of(String code, String message, Throwable cause) {
        return new AgentError(code, message, cause, Map.of());
    }
}
