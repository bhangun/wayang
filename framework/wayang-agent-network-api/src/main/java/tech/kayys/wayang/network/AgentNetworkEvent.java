package tech.kayys.wayang.network;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record AgentNetworkEvent(
        String networkTaskId,
        String executionId,
        AgentNetworkEventType type,
        Instant timestamp,
        Map<String, Object> data
) {

    public AgentNetworkEvent {
        Objects.requireNonNull(networkTaskId, "networkTaskId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(timestamp, "timestamp");
        data = data == null ? Map.of() : Map.copyOf(data);
    }

    public static AgentNetworkEvent of(String networkTaskId, String executionId, AgentNetworkEventType type, Map<String, Object> data) {
        return new AgentNetworkEvent(networkTaskId, executionId, type, Instant.now(), data);
    }
}
