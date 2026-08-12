package tech.kayys.wayang.provider;

import java.util.List;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;

/**
 * Strategy for selecting the most appropriate provider for an execution.
 */
public interface ModelRouter {
    
    /**
     * Selects a provider from the available list based on the agent definition and the specific request.
     *
     * @param request The specific request to route
     * @param agentDefinition The definition of the agent executing the request
     * @param availableProviders The list of providers configured in the system
     * @return The best matching provider
     * @throws IllegalStateException if no suitable provider can be found
     */
    Provider route(AgentRequest request, AgentDefinition agentDefinition, List<Provider> availableProviders);
}
