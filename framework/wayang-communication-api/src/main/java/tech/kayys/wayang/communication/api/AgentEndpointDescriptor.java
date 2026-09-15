package tech.kayys.wayang.communication.api;

import tech.kayys.wayang.communication.endpoint.AgentEndpoint;

import java.util.Map;
import java.util.Objects;

/**
 * Describes a single transport endpoint exposed by an agent.
 * Protocol and version identify the binding (e.g. "HTTP+JSON", "GRPC", "LOCAL").
 */
public record AgentEndpointDescriptor(
        AgentEndpoint endpoint,
        String protocol,
        String version,
        Map<String, Object> metadata
) {

    public AgentEndpointDescriptor {
        Objects.requireNonNull(endpoint, "endpoint");

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static AgentEndpointDescriptor of(
            AgentEndpoint endpoint,
            String protocol,
            String version
    ) {
        return new AgentEndpointDescriptor(endpoint, protocol, version, Map.of());
    }
}
