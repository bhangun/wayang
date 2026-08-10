package tech.kayys.wayang.service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.skill.spi.SkillDefinition;
import tech.kayys.wayang.workflow.WorkflowDefinition;
import tech.kayys.wayang.identity.ResourceId.CorrelationId;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionState;

/**
 * Execution Service - executes definitions
 */
public interface ExecutionService {
    ExecutionContext startAgent(String agentId, Map<String, Object> inputs);
    ExecutionContext startSkill(String skillId, Map<String, Object> inputs);
    ExecutionContext startWorkflow(String workflowId, Map<String, Object> inputs);
    
    ExecutionContext startAgent(AgentDefinition agent, Map<String, Object> inputs);
    ExecutionContext startSkill(SkillDefinition skill, Map<String, Object> inputs);
    ExecutionContext startWorkflow(WorkflowDefinition workflow, Map<String, Object> inputs);
    
    CompletableFuture<ExecutionContext> startAgentAsync(String agentId, Map<String, Object> inputs);
    CompletableFuture<ExecutionContext> startSkillAsync(String skillId, Map<String, Object> inputs);
    CompletableFuture<ExecutionContext> startWorkflowAsync(String workflowId, Map<String, Object> inputs);
    
    Optional<ExecutionContext> getExecution(CorrelationId correlationId);
    Optional<ExecutionContext> getExecution(String correlationId);
    List<ExecutionContext> listExecutions();
    List<ExecutionContext> listExecutions(ExecutionState state);
    
    void pauseExecution(CorrelationId correlationId);
    void resumeExecution(CorrelationId correlationId);
    void cancelExecution(CorrelationId correlationId);
}
