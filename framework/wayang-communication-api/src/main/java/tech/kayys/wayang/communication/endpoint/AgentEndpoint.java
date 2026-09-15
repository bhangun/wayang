package tech.kayys.wayang.communication.endpoint;

public sealed interface AgentEndpoint
        permits LocalAgentEndpoint, RemoteAgentEndpoint {

    EndpointType type();
}
