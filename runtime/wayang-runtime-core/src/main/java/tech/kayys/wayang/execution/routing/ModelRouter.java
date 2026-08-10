package tech.kayys.wayang.execution.routing;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.execution.ExecutionStatus;
import tech.kayys.wayang.inference.ModelInfo;

/**
 * Selects the best AI model for a given agent turn.
 *
 * <p>Routing dimensions include: task complexity, cost, latency, context size,
 * tool-calling support, multi-modal needs, availability, tenant policy, and
 * data residency.</p>
 *
 * <p>Implementations are CDI beans and are discovered automatically.</p>
 */
public interface ModelRouter {

    /**
     * Selects the best model given the current execution context and routing
     * criteria.
     *
     * @param context  the semantic agent state (history, variables, artifacts)
     * @param selector the routing criteria for this turn
     * @return the selected {@link ModelInfo}; never {@code null}
     */
    ModelInfo select(AgentContext context, ModelSelector selector);

    /**
     * Selects the fallback model when the primary selection fails.
     *
     * @param context  the semantic agent state
     * @param failed   the model that failed
     * @param selector the original routing criteria
     * @return the fallback {@link ModelInfo}, or {@code null} if no fallback exists
     */
    default ModelInfo fallback(AgentContext context, ModelInfo failed, ModelSelector selector) {
        return null;
    }
}
