package tech.kayys.wayang.execution;

import tech.kayys.wayang.communication.api.AgentResponse;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the successful result of an agent invocation.
 */
public record AgentResult(
        AgentResponse response,
        Map<String, Object> metadata
) {
    public AgentResult {
        Objects.requireNonNull(response, "response must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static AgentResult of(AgentResponse response) {
        return new AgentResult(response, Map.of());
    }

    public static AgentResult of(AgentResponse response, Map<String, Object> metadata) {
        return new AgentResult(response, metadata);
    }
}
