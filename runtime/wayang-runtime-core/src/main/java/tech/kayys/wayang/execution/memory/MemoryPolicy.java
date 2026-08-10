package tech.kayys.wayang.execution.memory;

import tech.kayys.wayang.agent.AgentContext;

import java.util.List;

/**
 * Policy that controls what the model receives from memory on each turn and
 * what gets written back after the turn completes.
 *
 * <p>The LLM should <em>not</em> receive all history. Instead:</p>
 * <ul>
 *   <li>Relevant working state</li>
 *   <li>Relevant episodic memories</li>
 *   <li>Relevant semantic facts</li>
 *   <li>Relevant artifacts</li>
 * </ul>
 */
public interface MemoryPolicy {

    /**
     * Retrieves memory items relevant to the current execution context.
     *
     * @param context the current execution state
     * @param layer   the memory layer to query
     * @param query   the natural language query for semantic search (may be null)
     * @param limit   maximum number of items to return
     * @return a list of relevant memory items as strings
     */
    List<String> retrieve(AgentContext context, MemoryLayer layer, String query, int limit);

    /**
     * Writes a new memory item to the specified layer.
     *
     * @param context the current execution state
     * @param layer   the target memory layer
     * @param content the content to persist
     */
    void write(AgentContext context, MemoryLayer layer, String content);
}
