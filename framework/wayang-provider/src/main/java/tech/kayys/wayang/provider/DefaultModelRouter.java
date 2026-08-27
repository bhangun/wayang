package tech.kayys.wayang.provider;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.ContextRequirements;
import tech.kayys.wayang.provider.routing.*;

import java.util.List;

/**
 * Configurable ModelRouter supporting both DIRECT (strict explicit match)
 * and ADAPTIVE (multi-criteria capability, budget, and telemetry scoring) strategies.
 */
public class DefaultModelRouter implements ModelRouter {

    public static final String ROUTER_STRATEGY_PROP = "wayang.router.strategy";

    private final RoutingStrategy strategy;
    private final DirectModelRouter directRouter;
    private final AdaptiveModelRouter adaptiveRouter;

    public DefaultModelRouter() {
        this(resolveConfiguredStrategy());
    }

    public DefaultModelRouter(RoutingStrategy strategy) {
        this.strategy = strategy != null ? strategy : RoutingStrategy.DIRECT;
        this.directRouter = new DirectModelRouter();
        this.adaptiveRouter = new AdaptiveModelRouter();
    }

    public DefaultModelRouter(RoutingStrategy strategy, ModelRegistry modelRegistry, ModelScorer modelScorer, ModelRoutingTelemetry telemetry) {
        this.strategy = strategy != null ? strategy : RoutingStrategy.DIRECT;
        this.directRouter = new DirectModelRouter();
        this.adaptiveRouter = new AdaptiveModelRouter(modelRegistry, modelScorer, telemetry);
    }

    public RoutingStrategy getStrategy() {
        return strategy;
    }

    @Override
    public InferencePlan plan(
            AgentRequest request,
            AgentDefinition agentDefinition,
            InferenceRequirements requirements,
            InferencePolicy policy,
            List<Provider> availableProviders
    ) {
        if (strategy == RoutingStrategy.ADAPTIVE) {
            return adaptiveRouter.plan(request, agentDefinition, requirements, policy, availableProviders);
        } else {
            return directRouter.plan(request, agentDefinition, requirements, policy, availableProviders);
        }
    }

    @Override
    public Provider route(AgentRequest request, AgentDefinition agentDefinition, List<Provider> availableProviders) {
        if (strategy == RoutingStrategy.ADAPTIVE) {
            return adaptiveRouter.route(request, agentDefinition, availableProviders);
        } else {
            return directRouter.route(request, agentDefinition, availableProviders);
        }
    }

    @Override
    public Provider route(ContextRequirements requirements, List<Provider> availableProviders) {
        if (strategy == RoutingStrategy.ADAPTIVE) {
            return adaptiveRouter.route(requirements, availableProviders);
        } else {
            return directRouter.route(requirements, availableProviders);
        }
    }

    private static RoutingStrategy resolveConfiguredStrategy() {
        String prop = System.getProperty(ROUTER_STRATEGY_PROP);
        if (prop == null || prop.isBlank()) {
            prop = System.getenv("WAYANG_ROUTER_STRATEGY");
        }
        if ("adaptive".equalsIgnoreCase(prop) || "advanced".equalsIgnoreCase(prop)) {
            return RoutingStrategy.ADAPTIVE;
        }
        return RoutingStrategy.DIRECT;
    }
}

