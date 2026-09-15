package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.message.MessagePayload;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public record AgentRequest(
        AgentRef target,
        CapabilityId capability,
        MessagePayload payload,
        RequestMode mode,
        Duration timeout,
        CommunicationOptions communication,
        Map<String, Object> metadata
) {

    public AgentRequest {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(capability, "capability");
        Objects.requireNonNull(payload, "payload");

        mode = mode == null
                ? RequestMode.AUTO
                : mode;

        timeout = timeout == null
                ? Duration.ofSeconds(60)
                : timeout;

        communication = communication == null
                ? CommunicationOptions.automatic()
                : communication;

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public AgentRequest(
            AgentRef target,
            CapabilityId capability,
            MessagePayload payload,
            RequestMode mode,
            Duration timeout,
            Map<String, Object> metadata
    ) {
        this(target, capability, payload, mode, timeout, CommunicationOptions.automatic(), metadata);
    }

    public static AgentRequest of(AgentRef target, CapabilityId capability, MessagePayload payload) {
        return new AgentRequest(target, capability, payload, RequestMode.AUTO, Duration.ofSeconds(60), CommunicationOptions.automatic(), Map.of());
    }
}
