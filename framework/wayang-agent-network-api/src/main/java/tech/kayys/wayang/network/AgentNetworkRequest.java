package tech.kayys.wayang.network;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Top-level invocation request across the agent network.
 */
public record AgentNetworkRequest(
        AgentRequest request,
        SecurityContextSnapshot securityContext,
        Duration timeout,
        Map<String, Object> metadata
) {

    public AgentNetworkRequest {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(securityContext, "securityContext");
        timeout  = timeout == null ? Duration.ofSeconds(30) : timeout;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static AgentNetworkRequest of(AgentRequest request, SecurityContextSnapshot securityContext) {
        return new AgentNetworkRequest(request, securityContext, Duration.ofSeconds(30), Map.of());
    }
}
