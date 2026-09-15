package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.message.MessagePayload;

import java.util.Map;

public record AgentResponse(
        boolean success,
        MessagePayload payload,
        Map<String, Object> metadata,
        AgentError error
) {

    public AgentResponse {
        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static AgentResponse success(MessagePayload payload) {
        return new AgentResponse(true, payload, Map.of(), null);
    }

    public static AgentResponse success(MessagePayload payload, Map<String, Object> metadata) {
        return new AgentResponse(true, payload, metadata, null);
    }

    public static AgentResponse failure(AgentError error) {
        return new AgentResponse(false, null, Map.of(), error);
    }

    public static AgentResponse failure(String code, String message) {
        return failure(AgentError.of(code, message));
    }
}
