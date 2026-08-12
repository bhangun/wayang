package tech.kayys.wayang.a2a.api;

import java.util.concurrent.CompletionStage;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2ATask;
import tech.kayys.wayang.a2a.model.AgentCard;

/**
 * Defines the server-side interaction for exposing a local Wayang agent via A2A.
 */
public interface A2AServer {

    /**
     * Gets the agent card describing the exposed agent.
     * @return The agent card.
     */
    AgentCard getAgentCard();

    /**
     * Receives a message and returns the created or updated task.
     * @param message The message to process.
     * @return A CompletionStage resolving to the associated task.
     */
    CompletionStage<A2ATask> sendMessage(A2AMessage message);

    /**
     * Retrieves the current status of an A2A task by its ID.
     * @param taskId The task ID.
     * @return A CompletionStage resolving to the task.
     */
    CompletionStage<A2ATask> getTask(String taskId);
    
    /**
     * Cancels an ongoing A2A task.
     * @param taskId The task ID.
     * @return A CompletionStage resolving when the task is cancelled.
     */
    CompletionStage<Void> cancelTask(String taskId);
}
