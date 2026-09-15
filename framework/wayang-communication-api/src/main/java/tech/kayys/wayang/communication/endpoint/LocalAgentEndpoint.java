package tech.kayys.wayang.communication.endpoint;

import java.util.Objects;

public record LocalAgentEndpoint(
        String runtimeId
) implements AgentEndpoint {

    public LocalAgentEndpoint {
        Objects.requireNonNull(runtimeId, "runtimeId");
        if (runtimeId.isBlank()) {
            throw new IllegalArgumentException("runtimeId must not be blank");
        }
    }

    public static LocalAgentEndpoint of(String runtimeId) {
        return new LocalAgentEndpoint(runtimeId);
    }

    @Override
    public EndpointType type() {
        return EndpointType.LOCAL;
    }
}
