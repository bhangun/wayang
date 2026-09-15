package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentEndpointDescriptor;

/**
 * Translates a Wayang {@link AgentEndpointDescriptor} into an {@link A2AInterface}.
 * Isolates protocol-specific endpoint translation.
 */
public interface A2AEndpointBindingMapper {

    boolean supports(AgentEndpointDescriptor endpoint);

    A2AInterface map(AgentEndpointDescriptor endpoint);
}
