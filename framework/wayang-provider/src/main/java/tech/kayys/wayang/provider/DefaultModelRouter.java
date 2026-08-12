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
        
        // Phase 3 implementation: Simple fallback routing.
        // In the future, this would inspect agentDefinition.constraints() and Provider capabilities.
        
        // For now, return the first one
        return availableProviders.get(0);
    }
}
