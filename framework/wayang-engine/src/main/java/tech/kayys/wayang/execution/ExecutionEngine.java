package tech.kayys.wayang.execution;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.definition.AgentDefinition;
import tech.kayys.wayang.definition.SkillDefinition;
import tech.kayys.wayang.definition.WorkflowDefinition;
import tech.kayys.wayang.extension.Extension;

/**
 * Execution Engine - executes agents and workflows.
 */
public interface ExecutionEngine extends Extension {
    
    /**
     * Execute an agent
     */
    ExecutionResult executeAgent(AgentDefinition agent, ExecutionContext context) throws Exception;
    
    /**
     * Execute an agent with inputs
     */
    default ExecutionResult executeAgent(AgentDefinition agent, ExecutionContext context, 
            Map<String, Object> inputs) throws Exception {
        return executeAgent(agent, context);
    }
    
    /**
     * Execute a workflow
     */
    ExecutionResult executeWorkflow(WorkflowDefinition workflow, ExecutionContext context) throws Exception;
    
    /**
     * Execute a skill
     */
    ExecutionResult executeSkill(SkillDefinition skill, ExecutionContext context) throws Exception;
    
    /**
     * Execute asynchronously
     */
    default CompletableFuture<ExecutionResult> executeAsync(AgentDefinition agent, 
            ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeAgent(agent, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Pause execution
     */
    default void pause(String executionId) throws Exception {
        // Optional
    }
    
    /**
     * Resume execution
     */
    default void resume(String executionId) throws Exception {
        // Optional
    }
    
    /**
     * Cancel execution
     */
    default void cancel(String executionId) throws Exception {
        // Optional
    }
    
    /**
     * Get execution status
     */
    default ExecutionStatus getStatus(String executionId) throws Exception {
        return ExecutionStatus.UNKNOWN;
    }
    
    /**
     * Get execution result
     */
    default Optional<ExecutionResult> getResult(String executionId) throws Exception {
        return Optional.empty();
    }
}

