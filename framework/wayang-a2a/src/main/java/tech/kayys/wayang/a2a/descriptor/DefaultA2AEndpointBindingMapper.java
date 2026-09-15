package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentEndpointDescriptor;

/**
 * Default endpoint binding mapper that converts any endpoint with a non-null protocol.
 */
public final class DefaultA2AEndpointBindingMapper implements A2AEndpointBindingMapper {

    @Override
    public boolean supports(AgentEndpointDescriptor endpoint) {
        return endpoint.protocol() != null && !endpoint.protocol().isBlank();
    }

    @Override
    public A2AInterface map(AgentEndpointDescriptor endpoint) {
        String url = resolveUrl(endpoint);
        return new A2AInterface(
                url,
                endpoint.protocol(),
                endpoint.version() != null ? endpoint.version() : "1.0",
                null
        );
    }

    private String resolveUrl(AgentEndpointDescriptor endpoint) {
        if (endpoint.endpoint() instanceof tech.kayys.wayang.communication.endpoint.RemoteAgentEndpoint remote) {
            return remote.uri().toString();
        }
        return null;
    }
}
