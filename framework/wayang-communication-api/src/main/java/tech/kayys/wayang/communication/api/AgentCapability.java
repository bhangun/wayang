package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.capability.CapabilityId;

import java.util.Map;
import java.util.Objects;

public record AgentCapability(
        CapabilityId id,
        String description,
        Map<String, Object> metadata
) {

    public AgentCapability {
        Objects.requireNonNull(id, "id");

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static AgentCapability of(String capabilityId, String description) {
        return new AgentCapability(CapabilityId.of(capabilityId), description, Map.of());
    }
}
