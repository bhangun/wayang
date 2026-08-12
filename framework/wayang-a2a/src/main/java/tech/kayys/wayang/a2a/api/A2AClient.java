package tech.kayys.wayang.a2a.api;

import java.util.concurrent.CompletionStage;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2ATask;

/**
 * Defines the client-side interaction with a remote A2A agent.
 */
public interface A2AClient {

    /**
     * Sends a message to the remote agent and returns the created or updated task.
     * @param message The message to send.
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
