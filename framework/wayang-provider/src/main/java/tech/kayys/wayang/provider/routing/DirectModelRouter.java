package tech.kayys.wayang.provider.routing;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.ContextRequirements;
import tech.kayys.wayang.provider.ModelRouter;
import tech.kayys.wayang.provider.Provider;

import java.util.List;
import java.util.UUID;

/**
 * Direct / Regular ModelRouter strategy.
 * Strictly honors explicitly requested models without budget or adaptive scoring interference.
 */
public class DirectModelRouter implements ModelRouter {

    @Override
    public InferencePlan plan(
            AgentRequest request,
            AgentDefinition agentDefinition,
            InferenceRequirements requirements,
            InferencePolicy policy,
            List<Provider> availableProviders
    ) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No available providers to route to");
        }

        String requestId = (request != null && request.id() != null) ? request.id() : "req-" + UUID.randomUUID();

        // 1. Explicit model request check
        if (agentDefinition != null && agentDefinition.model() != null) {
            String requestedModel = (agentDefinition.model().name() != null && !agentDefinition.model().name().isBlank())
                    ? agentDefinition.model().name()
                    : agentDefinition.model().id().asString();
            for (Provider provider : availableProviders) {
                if (provider.id() != null && provider.id().equalsIgnoreCase(requestedModel)) {
                    return InferencePlan.direct(requestId, requestedModel, provider,
                            "Direct explicit match on provider ID: " + requestedModel);
                }
                if (provider.getClass().getSimpleName().toLowerCase().contains(requestedModel.toLowerCase())) {
                    return InferencePlan.direct(requestId, requestedModel, provider,
                            "Direct explicit match on provider class: " + requestedModel);
                }
            }
            // If requested model isn't matched by class name, route to first provider with requested model name
            return InferencePlan.direct(requestId, requestedModel, availableProviders.get(0),
                    "Direct route with requested model: " + requestedModel);
        }

        // 2. Fallback to first available provider
        Provider fallback = availableProviders.get(0);
        String modelName = fallback.id() != null ? fallback.id() : fallback.getClass().getSimpleName();
        return InferencePlan.direct(requestId, modelName, fallback,
                "Direct route defaulting to first available provider");
    }

    @Override
    public Provider route(AgentRequest request, AgentDefinition agentDefinition, List<Provider> availableProviders) {
        return plan(request, agentDefinition, InferenceRequirements.defaults(), InferencePolicy.defaults(), availableProviders).selectedProvider();
    }

    @Override
    public Provider route(ContextRequirements requirements, List<Provider> availableProviders) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No available providers to route to");
        }
        return availableProviders.stream()
                .filter(p -> p.supportedModalities().containsAll(requirements.modalities()))
                .findFirst()
                .orElse(availableProviders.get(0));
    }
}
