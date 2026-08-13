package tech.kayys.wayang.provider;

import java.util.List;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;

/**
 * Basic implementation of ModelRouter that defaults to the first available provider.
 * More advanced implementations can filter by capability, cost, or availability.
 */
public class DefaultModelRouter implements ModelRouter {
    
    @Override
    public Provider route(AgentRequest request, AgentDefinition agentDefinition, List<Provider> availableProviders) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No available providers to route to");
        }
        
        // If agent explicitly requests a specific model reference, try to find a matching provider.
        if (agentDefinition != null && agentDefinition.model() != null) {
            String requestedModel = agentDefinition.model().id().asString();
            for (Provider provider : availableProviders) {
                // In a fuller implementation, Provider would expose capabilities and models it supports.
                // For now, we perform a naive check on the provider's class name or ID.
                if (provider.getClass().getSimpleName().toLowerCase().contains(requestedModel.toLowerCase())) {
                    return provider;
                }
            }
        }
        
        // Phase 4 implementation: Simple fallback routing if no specific constraints match.
        // In the future, this would inspect agentDefinition.constraints() vs Provider capabilities.
        return availableProviders.get(0);
    }
}
