package tech.kayys.wayang.execution.memory;

import tech.kayys.wayang.agent.AgentContext;

import java.util.List;

/**
 * Orchestrates memory retrieval and persistence across all four
 * {@link MemoryLayer}s for a given agent turn.
 */
public interface MemoryManager {

    /**
     * Retrieves a merged, ranked set of relevant memory items from all layers.
     * The result is ready to be inserted into a prompt by the ContextPlanner.
     *
     * @param context the current execution state
     * @param query   the user request / current goal
     * @param limit   max total items across all layers
     * @return a list of relevant memory fragments
     */
    List<String> retrieve(AgentContext context, String query, int limit);

    /**
     * Persists a completed turn's output across the appropriate memory layers.
     *
     * @param context   the context at the start of the turn
     * @param turnInput the user input / model prompt for this turn
     * @param turnOutput the model/tool output for this turn
     */
    void writeTurn(AgentContext context, String turnInput, String turnOutput);

    /**
     * Retrieves all working-memory entries for the current session.
     */
    List<String> workingMemory(AgentContext context);
}
