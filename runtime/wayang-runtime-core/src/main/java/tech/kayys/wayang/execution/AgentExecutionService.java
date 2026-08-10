package tech.kayys.wayang.execution;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.Id;

/**
 * Service for creating and managing AgentExecutions.
 */
@ApplicationScoped
public class AgentExecutionService {

    @Inject
    CheckpointStore checkpointStore;
    
    @Inject
    AgentToolExecutor toolExecutor;

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
}
