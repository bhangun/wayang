package tech.kayys.wayang.provider;

import java.util.List;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.ContextRequirements;

/**
 * Strategy for selecting the most appropriate provider for an execution.
 */
import tech.kayys.wayang.provider.routing.InferencePlan;
import tech.kayys.wayang.provider.routing.InferencePolicy;
import tech.kayys.wayang.provider.routing.InferenceRequirements;

/**
 * Strategy for selecting the most appropriate model and provider for an execution.
 */
public interface ModelRouter {
    
    /**
     * Generates a comprehensive inference plan for executing the agent request under policy and budget constraints.
     *
     * @param request The specific agent request to route
     * @param agentDefinition The definition of the agent executing the request
     * @param requirements The derived context requirements
     * @param policy The inference policy and optimization objective
     * @param availableProviders The list of providers configured in the system
     * @return The complete, explainable inference plan
     */
    default InferencePlan plan(
            AgentRequest request,
            AgentDefinition agentDefinition,
            InferenceRequirements requirements,
            InferencePolicy policy,
            List<Provider> availableProviders
    ) {
        Provider p = route(request, agentDefinition, availableProviders);
        String modelId = (agentDefinition != null && agentDefinition.model() != null)
                ? agentDefinition.model().id().asString()
                : p.id() != null ? p.id() : p.getClass().getSimpleName();
        return InferencePlan.direct(
                request != null ? request.id() : null,
                modelId,
                p,
                "Default route delegation"
        );
    }

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

    /**
     * Selects a provider from the available list based on the explicit context requirements.
     *
     * @param requirements The explicitly defined context requirements (modalities, tool calling)
     * @param availableProviders The list of providers configured in the system
     * @return The best matching provider
     * @throws IllegalStateException if no suitable provider can be found
     */
    default Provider route(ContextRequirements requirements, List<Provider> availableProviders) {
        return availableProviders.stream()
                .filter(p -> p.supportedModalities().containsAll(requirements.modalities()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No suitable provider found for requirements: " + requirements));
    }
}
