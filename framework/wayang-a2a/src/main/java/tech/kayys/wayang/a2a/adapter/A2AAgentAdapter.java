package tech.kayys.wayang.a2a.adapter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import tech.kayys.wayang.a2a.api.A2AServer;
import tech.kayys.wayang.a2a.model.A2AMessage;
import tech.kayys.wayang.a2a.model.A2ATask;
import tech.kayys.wayang.a2a.model.AgentCard;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.core.runtime.WayangRuntime;

/**
 * Adapter that maps A2A Server requests to the internal AgentExecution engine.
 */
public class A2AAgentAdapter implements A2AServer {

    private final WayangRuntime runtime;
    private final AgentDefinition agentDefinition;
    
    // In-memory track of active tasks
    private final Map<String, CompletableFuture<AgentResponse>> activeExecutions = new ConcurrentHashMap<>();

    public A2AAgentAdapter(WayangRuntime runtime, AgentDefinition agentDefinition) {
        this.runtime = runtime;
        this.agentDefinition = agentDefinition;
    }

    @Override
    public AgentCard getAgentCard() {
        return new AgentCard(
            agentDefinition.id().asString(),
            agentDefinition.metadata().name(),
            agentDefinition.metadata().description(),
            java.util.List.of(), // capabilities
            java.util.List.of(), // skills
            java.util.List.of(), // modalities
            java.util.Map.of(),  // authentication
            null,                // endpoint
            java.util.Map.of()   // metadata
        );
    }

    @Override
    public CompletionStage<A2ATask> sendMessage(A2AMessage message) {
        AgentRequest request = A2AMessageMapper.toAgentRequest(message);
        CompletableFuture<AgentResponse> executionFuture = runtime.executeAsync(agentDefinition, request);
        
        String taskId = UUID.randomUUID().toString();
        activeExecutions.put(taskId, executionFuture);
        
        return CompletableFuture.completedFuture(A2AMessageMapper.toInProgressTask(taskId));
    }

    @Override
    public CompletionStage<A2ATask> getTask(String taskId) {
        CompletableFuture<AgentResponse> future = activeExecutions.get(taskId);
        
        if (future == null) {
            return CompletableFuture.completedFuture(A2AMessageMapper.toFailedTask(taskId, new RuntimeException("Task not found or expired: " + taskId)));
        }
        
        if (!future.isDone()) {
            return CompletableFuture.completedFuture(A2AMessageMapper.toInProgressTask(taskId));
        }
        
        return future.handle((response, ex) -> {
            activeExecutions.remove(taskId);
            if (ex != null) {
                return A2AMessageMapper.toFailedTask(taskId, ex);
            } else {
                return A2AMessageMapper.toCompletedTask(taskId, response);
            }
        });
    }

    @Override
    public CompletionStage<Void> cancelTask(String taskId) {
        CompletableFuture<AgentResponse> future = activeExecutions.remove(taskId);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        return CompletableFuture.completedFuture(null);
    }
}
