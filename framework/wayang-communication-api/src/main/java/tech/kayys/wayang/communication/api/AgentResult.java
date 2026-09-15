package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.message.MessagePayload;

import java.util.Map;

public record AgentResult(
        boolean success,
        MessagePayload payload,
        Map<String, Object> metadata,
        AgentError error
) {

    public AgentResult {
        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static AgentResult success(MessagePayload payload) {
        return new AgentResult(true, payload, Map.of(), null);
    }

    public static AgentResult success(MessagePayload payload, Map<String, Object> metadata) {
        return new AgentResult(true, payload, metadata, null);
    }

    public static AgentResult failure(AgentError error) {
        return new AgentResult(false, null, Map.of(), error);
    }

    public static AgentResult failure(Throwable error) {
        return new AgentResult(false, null, Map.of(), AgentError.of("EXECUTION_ERROR", error != null ? error.getMessage() : "unknown"));
    }

    public static AgentResult failure(String code, String message) {
        return new AgentResult(false, null, Map.of(), AgentError.of(code, message));
    }

    public static AgentResult from(AgentResponse response) {
        return new AgentResult(response.success(), response.payload(), response.metadata(), response.error());
    }
}
