package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Agent session interface.
 */
public interface AgentSession {

    /**
     * Returns the session ID.
     */
    UUID id();

    /**
     * Returns the agent definition.
     */
    AgentDefinition definition();

    /**
     * Returns the agent state.
     */
    AgentState state();

    /**
     * Returns the conversation.
     */
    Conversation conversation();

    /**
     * Returns the working memory.
     */
    WorkingMemory workingMemory();

    /**
     * Returns the execution history.
     */
    ExecutionHistory history();

    /**
     * Returns the agent variables.
     */
    AgentVariables variables();

    /**
     * Returns the session metadata.
     */
    Map<String, Object> metadata();

    /**
     * Returns the session creation time.
     */
    Instant createdAt();

    /**
     * Returns the last activity time.
     */
    Instant lastActivityAt();

    /**
     * Updates the session state.
     */
    AgentSession withState(AgentState state);

    /**
     * Adds a message to the conversation.
     */
    AgentSession addMessage(Message message);

    /**
     * Adds an execution record.
     */
    AgentSession addExecution(ExecutionRecord record);

    /**
     * Sets a variable.
     */
    AgentSession setVariable(String key, Object value);

    /**
     * Gets a variable.
     */
    Optional<Object> getVariable(String key);

    /**
     * Returns the user ID.
     */
    default String userId() {
        return definition() != null ? definition().getId() : null;
    }
}