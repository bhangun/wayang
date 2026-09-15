package tech.kayys.wayang.network.core.router;

import tech.kayys.wayang.network.endpoint.ResolvedEndpoint;
import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;

import java.util.Objects;

public record NetworkCandidate(
        ResolvedEndpoint endpoint,
        AgentNetworkProtocol protocol,
        int priority
) {
    public NetworkCandidate {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(protocol, "protocol");
    }
}
