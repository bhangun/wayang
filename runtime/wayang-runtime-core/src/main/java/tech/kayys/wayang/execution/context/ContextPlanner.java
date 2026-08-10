package tech.kayys.wayang.execution.context;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.inference.ModelInfo;

/**
 * Plans and compiles the context that will be sent to the model on each turn.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Allocate token budget across layers (system, conversation, memory, RAG, artifacts)</li>
 *   <li>Retrieve relevant content from each {@code ContextProvider}</li>
 *   <li>Trim / summarize each layer to fit within its budget</li>
 *   <li>Return a {@link ContextPlan} ready for injection into the prompt</li>
 * </ul>
 */
public interface ContextPlanner {

    /**
     * Plans the context for the next model invocation.
     *
     * @param context the semantic agent state
     * @param model   the model that will receive the context (used to pick budget)
     * @param budget  explicit budget override; pass {@code null} to auto-select
     * @return a compiled {@link ContextPlan}
     */
    ContextPlan plan(AgentContext context, ModelInfo model, ContextBudget budget);

    /**
     * Plans with auto-selected budget based on the model's context window.
     */
    default ContextPlan plan(AgentContext context, ModelInfo model) {
        return plan(context, model, null);
    }
}
