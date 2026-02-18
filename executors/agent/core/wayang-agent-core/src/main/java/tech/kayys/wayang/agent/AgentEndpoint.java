package tech.kayys.wayang.agent;

import java.util.Map;

/**
 * Agent Endpoint - Connection details for an agent.
 */
public record AgentEndpoint(
        EndpointType type,
        String address,
        Map<String, String> connectionParams) {
}
