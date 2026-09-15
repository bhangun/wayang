package tech.kayys.wayang.network;

import tech.kayys.wayang.communication.api.AgentResponse;

import java.util.Map;
import java.util.Objects;

public record AgentNetworkResponse(
        AgentResponse response,
        Map<String, Object> metadata
) {

    public AgentNetworkResponse {
        Objects.requireNonNull(response, "response");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static AgentNetworkResponse of(AgentResponse response) {
        return new AgentNetworkResponse(response, Map.of());
    }

    public boolean success() {
        return response.success();
    }
}
