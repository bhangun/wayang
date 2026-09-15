package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.execution.AgentInvocation;
import tech.kayys.wayang.execution.ExecutionContext;

import java.util.Objects;

/**
 * Default implementation of {@link AgentInvocation}.
 */
public record DefaultAgentInvocation(
        AgentRequest request,
        ExecutionContext context
) implements AgentInvocation {

    public DefaultAgentInvocation {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(context, "context must not be null");
    }
}
