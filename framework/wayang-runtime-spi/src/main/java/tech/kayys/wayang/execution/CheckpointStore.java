package tech.kayys.wayang.execution;

import java.util.List;
import java.util.Optional;

import tech.kayys.wayang.agent.AgentContext;

/**
 * Pluggable strategy for persisting and recovering execution checkpoints.
 */
public interface CheckpointStore {
    
    /**
     * Saves a checkpoint of the given execution context.
     * 
     * @param executionId The execution ID
     * @param context The semantic agent state to save
     */
    void save(String executionId, AgentContext context);
    
    /**
     * Loads the latest checkpoint for the given execution ID.
     * 
     * @param executionId The execution ID
     * @return The restored agent context, if found
     */
    Optional<AgentContext> load(String executionId);
    
    /**
     * Retrieves the history of checkpoints for the given execution ID.
     * 
     * @param executionId The execution ID
     * @return A list of historical contexts
     */
    List<AgentContext> history(String executionId);
    
    /**
     * Deletes all checkpoints for the given execution ID.
     * 
     * @param executionId The execution ID
     */
    void delete(String executionId);
}
