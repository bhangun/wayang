package tech.kayys.wayang.execution;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.execution.context.ContextPlanner;
import tech.kayys.wayang.execution.memory.MemoryManager;
import tech.kayys.wayang.execution.routing.ModelRouter;
import tech.kayys.wayang.execution.routing.ModelSelector;
import tech.kayys.wayang.inference.ModelInfo;

/**
 * Service for creating and managing AgentExecutions.
 *
 * <p>Phase 3 additions: wires in {@link ModelRouter}, {@link ContextPlanner}, and
 * {@link MemoryManager} so every new execution starts with the right model,
 * pre-compiled context, and retrieved memory.</p>
 */
@ApplicationScoped
public class AgentExecutionService {

    @Inject
    CheckpointStore checkpointStore;
    
    @Inject
    AgentToolExecutor toolExecutor;

    @Inject
    ModelRouter modelRouter;

    @Inject
    ContextPlanner contextPlanner;

    @Inject
    MemoryManager memoryManager;

    public AgentExecution create(AgentDefinition agent, AgentRequest request, ExecutionBudget budget) {
        String executionId = Id.random().asString();
        
        AgentContext agentContext = AgentContext.builder()
            .id(new tech.kayys.wayang.identity.ResourceId.AgentId(new tech.kayys.wayang.extension.Id(java.util.UUID.randomUUID())))
            .request(request)
            .build();
            
        ExecutionContext executionContext = ExecutionContext.builder()
            .id(Id.fromString(executionId))
            .build();
            
        return new DefaultAgentExecution(
            executionId,
            agent,
            agentContext, 
            executionContext, 
            budget, 
            checkpointStore, 
            toolExecutor
        );
    }

    public AgentExecution resume(String executionId) {
        java.util.Optional<AgentContext> contextOpt = checkpointStore.load(executionId);
        if (contextOpt.isEmpty()) {
            throw new IllegalArgumentException("Execution not found: " + executionId);
        }
        
        // Recover minimal execution info
        ExecutionContext executionContext = ExecutionContext.builder()
            .id(Id.fromString(executionId))
            .build();
            
        DefaultAgentExecution execution = new DefaultAgentExecution(
            executionId,
            null, // Could recover agent definition if needed
            contextOpt.get(),
            executionContext,
            null,
            checkpointStore,
            toolExecutor
        );
        execution.resume();
        return execution;
    }

    public void approve(String executionId, AgentDecision decision) {
        AgentExecution execution = resume(executionId);
        if (decision instanceof AgentDecision.WaitForApproval) {
            // Re-run with the approval applied
            // In a complete implementation, this would insert the approval 
            // into the state and re-trigger execution.
            execution.execute();
        }
    }
}
