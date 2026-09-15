package tech.kayys.wayang.execution;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.security.delegation.DelegationContext;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

/**
 * Factory for creating agent invocations.
 *
 * <p>Separates invocation construction from execution, enabling clean testing
 * and protocol adapters (A2A, ANP, local) to create invocations without
 * knowing the execution internals.
 */
public interface AgentInvocationFactory {

    /**
     * Creates a root invocation — the entry point of an execution tree.
     *
     * @param request  the agent request
     * @param security the security context snapshot for this execution
     * @return a new root invocation
     */
    AgentInvocation root(AgentRequest request, SecurityContextSnapshot security);

    /**
     * Creates a child invocation — delegated from a parent execution.
     *
     * @param parent     the parent invocation
     * @param request    the child agent request
     * @param delegation the attenuated delegation context for the child
     * @return a new child invocation
     */
    AgentInvocation child(
            AgentInvocation parent,
            AgentRequest request,
            DelegationContext delegation
    );
}
