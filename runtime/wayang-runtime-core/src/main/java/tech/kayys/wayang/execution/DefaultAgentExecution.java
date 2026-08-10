package tech.kayys.wayang.execution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.core.AgentDefinition;

public class DefaultAgentExecution implements AgentExecution {

    private final String id;
    private final AgentDefinition agent;
    private final AgentContext agentContext;
    private final ExecutionContext executionContext;
    private final ExecutionBudget budget;
    private final CheckpointStore checkpointStore;
    private final AgentToolExecutor toolExecutor;
    
    private ExecutionStatus status;

    public DefaultAgentExecution(
        String id,
        AgentDefinition agent,
        AgentContext agentContext,
        ExecutionContext executionContext,
        ExecutionBudget budget,
        CheckpointStore checkpointStore,
        AgentToolExecutor toolExecutor
    ) {
        this.id = id;
        this.agent = agent;
        this.agentContext = agentContext;
        this.executionContext = executionContext;
        this.budget = budget;
        this.checkpointStore = checkpointStore;
        this.toolExecutor = toolExecutor;
        this.status = ExecutionStatus.CREATED;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public AgentContext agentContext() {
        return agentContext;
    }

    @Override
    public ExecutionContext executionContext() {
        return executionContext;
    }

    @Override
    public ExecutionStatus status() {
        return status;
    }

    @Override
    public CompletionStage<AgentResponse> execute() {
        this.status = ExecutionStatus.RUNNING;
        
        // In Phase 1, we just return a stub success to verify wiring, 
        // because the real ReAct loop is in ReActAgent.java which we will 
        // integrate in a subsequent step or phase.
        // For now, this replaces the Thread.sleep(100) stub.
        
        AgentResponse response = AgentResponse.builder()
            .id(id)
            .success(true)
            .content("Execution completed successfully by DefaultAgentExecution")
            .build();
            
        this.status = ExecutionStatus.COMPLETED;
        checkpointStore.save(id, agentContext);
        
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public AgentResponse executeSync() {
        try {
            return execute().toCompletableFuture().join();
        } catch (Exception e) {
            this.status = ExecutionStatus.FAILED;
            return AgentResponse.builder()
                .id(id)
                .success(false)
                .error(e.getMessage())
                .build();
        }
    }

    @Override
    public AgentResponse join() {
        // Simple stub since execute() currently returns a completed future.
        // In a real implementation with a state machine, this would wait
        // on a lock or CountDownLatch until status is terminal.
        if (this.status == ExecutionStatus.COMPLETED || this.status == ExecutionStatus.FAILED || this.status == ExecutionStatus.CANCELLED) {
             return AgentResponse.builder().id(id).success(this.status == ExecutionStatus.COMPLETED).build();
        }
        return executeSync();
    }

    @Override
    public void pause() {
        this.status = ExecutionStatus.PAUSED;
        checkpointStore.save(id, agentContext);
    }

    @Override
    public void resume() {
        this.status = ExecutionStatus.RUNNING;
    }

    @Override
    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        checkpointStore.delete(id);
    }
}
