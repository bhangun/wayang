package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * A resolver that determines the fallback strategy dynamically.
 * It checks for per-request overrides, then per-agent configurations, and falls back to a global default.
 */
public class DynamicGuardrailFallbackResolver implements GuardrailFallbackResolver {

    private final GuardrailFallbackStrategy globalDefaultStrategy;

    public DynamicGuardrailFallbackResolver() {
        this.globalDefaultStrategy = new HitlEscalationFallbackStrategy();
    }

    public DynamicGuardrailFallbackResolver(GuardrailFallbackStrategy globalDefaultStrategy) {
        this.globalDefaultStrategy = globalDefaultStrategy;
    }

    @Override
    public GuardrailFallbackStrategy resolve(Agent agent, ToolInvocation invocation) {
        // TODO: Access actual Request Context (e.g. Vertx RoutingContext or ThreadLocal)
        // to check for "X-Wayang-Guardrail-Fallback" headers or request properties.
        String requestStrategy = getStrategyFromRequest();
        if (requestStrategy != null) {
            return instantiateStrategy(requestStrategy);
        }

        // TODO: Access Agent's configuration properties (e.g., agent.getConfig().get("guardrail.fallback"))
        // String agentStrategy = agent.getConfig().get("guardrail.fallback");
        // if (agentStrategy != null) return instantiateStrategy(agentStrategy);

        return globalDefaultStrategy;
    }

    private String getStrategyFromRequest() {
        // Placeholder for actual request context lookup
        return null;
    }

    private GuardrailFallbackStrategy instantiateStrategy(String strategyName) {
        if ("block".equalsIgnoreCase(strategyName)) {
            return new BlockFallbackStrategy();
        } else if ("hitl".equalsIgnoreCase(strategyName)) {
            return new HitlEscalationFallbackStrategy();
        }
        return globalDefaultStrategy;
    }
}
