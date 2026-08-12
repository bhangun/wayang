package tech.kayys.wayang.agent.core.memory;

import io.smallrye.mutiny.Uni;

/**
 * Service for managing an agent's memory: storing interactions and retrieving context prompts.
 *
 * <p>This interface bridges the agent runtime with the underlying memory storage.
 * Implementations may delegate to persistent stores, in-memory caches, or vector databases.
 */
public interface AgentMemoryService {

    /**
     * Stores an agent interaction (e.g. a user message/response pair) in memory.
     *
     * @param agentId   the agent performing the interaction
     * @param sessionId optional session ID (may be null)
     * @param userId    optional user ID (may be null)
     * @param userInput the user's input or prompt
     * @param response  the agent's response
     * @return a Uni that completes when the storage operation finishes
     */
    Uni<Void> storeInteraction(String agentId, String sessionId, String userId,
                               String userInput, String response);

    /**
     * Builds a context prompt from the most recent interactions for the given agent.
     *
     * @param agentId the agent whose context to retrieve
     * @param limit   max number of past interactions to include
     * @return a Uni containing the formatted context prompt string
     */
    Uni<String> getContextPrompt(String agentId, int limit);
}
