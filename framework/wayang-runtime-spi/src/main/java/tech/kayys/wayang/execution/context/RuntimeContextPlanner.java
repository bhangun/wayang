package tech.kayys.wayang.execution.context;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.execution.ExecutionBudget;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates multiple ContextProviders (Memory, Skills, Repo, etc.) 
 * to assemble the unified context state for an execution step.
 */
public interface RuntimeContextPlanner {

    /**
     * Executes the planning and compilation of context.
     *
     * @param context   The current agent context and state
     * @param budget    The execution budget (including token limits)
     * @param providers The available providers to orchestrate
     * @return A consolidated RuntimeContextPlan ready for the model request
     */
    default RuntimeContextPlan planContext(AgentContext context, ExecutionBudget budget, List<ContextProvider> providers) {
        return planContext(context, budget, providers, null);
    }

    /**
     * Executes the planning and compilation of context with an explicit prompt or query.
     */
    RuntimeContextPlan planContext(AgentContext context, ExecutionBudget budget, List<ContextProvider> providers, String query);

    /**
     * Asynchronously executes the planning and compilation of context.
     */
    default CompletableFuture<RuntimeContextPlan> planContextAsync(AgentContext context, ExecutionBudget budget, List<ContextProvider> providers) {
        return CompletableFuture.supplyAsync(() -> planContext(context, budget, providers));
    }

    /**
     * Asynchronously executes the planning and compilation of context with an explicit prompt or query.
     */
    default CompletableFuture<RuntimeContextPlan> planContextAsync(AgentContext context, ExecutionBudget budget, List<ContextProvider> providers, String query) {
        return CompletableFuture.supplyAsync(() -> planContext(context, budget, providers, query));
    }
}
