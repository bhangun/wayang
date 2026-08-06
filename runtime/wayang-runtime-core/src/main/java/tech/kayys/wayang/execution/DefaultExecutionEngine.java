package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.definition.AgentDefinition;
import tech.kayys.wayang.definition.SkillDefinition;
import tech.kayys.wayang.definition.WorkflowDefinition;
import tech.kayys.wayang.definition.WorkflowStep;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Default Execution Engine Implementation
 */
public class DefaultExecutionEngine implements ExecutionEngine {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, ExecutionResult> results = new ConcurrentHashMap<>();
    private final Map<String, ExecutionStatus> statuses = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, Future<?>> runningExecutions = new ConcurrentHashMap<>();
    
    public DefaultExecutionEngine() {
        this.id = Id.random().asString();
        this.name = "execution-engine";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Wayang Execution Engine")
            .version(version)
            .label("type", "execution")
            .now()
            .build();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("execution"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public ExecutionResult executeAgent(AgentDefinition agent, ExecutionContext context) 
            throws Exception {
        String executionId = context.id().asString();
        statuses.put(executionId, ExecutionStatus.RUNNING);
        
        try {
            // Validate agent
            if (agent == null) {
                throw new IllegalArgumentException("Agent definition is required");
            }
            
            // Execute agent logic
            List<Artifact> outputs = new ArrayList<>();
            
            // Here would be the actual agent execution logic
            // For now, simulate execution
            Thread.sleep(100);
            
            // Add a result
            ExecutionResult result = ExecutionResult.success(
                executionId, 
                agent.id().asString(), 
                "agent",
                outputs
            );
            
            results.put(executionId, result);
            statuses.put(executionId, ExecutionStatus.COMPLETED);
            
            return result;
            
        } catch (Exception e) {
            ExecutionResult result = ExecutionResult.failed(
                executionId, 
                agent.id().asString(), 
                e.getMessage()
            );
            results.put(executionId, result);
            statuses.put(executionId, ExecutionStatus.FAILED);
            throw e;
        }
    }
    
    @Override
    public ExecutionResult executeWorkflow(WorkflowDefinition workflow, ExecutionContext context) 
            throws Exception {
        String executionId = context.id().asString();
        statuses.put(executionId, ExecutionStatus.RUNNING);
        
        try {
            // Validate workflow
            if (workflow == null) {
                throw new IllegalArgumentException("Workflow definition is required");
            }
            
            // Execute workflow steps
            List<ExecutionStepResult> stepResults = new ArrayList<>();
            List<Artifact> outputs = new ArrayList<>();
            
            for (WorkflowStep step : workflow.steps()) {
                ExecutionStepResult stepResult = executeWorkflowStep(step, context);
                stepResults.add(stepResult);
                if (stepResult.status() == ExecutionStatus.COMPLETED) {
                    outputs.addAll(stepResult.outputs());
                }
            }
            
            ExecutionResult result = ExecutionResult.success(
                executionId, 
                workflow.id().asString(), 
                "workflow",
                outputs
            );
            
            for (ExecutionStepResult stepResult : stepResults) {
                result = result.withStep(stepResult);
            }
            
            results.put(executionId, result);
            statuses.put(executionId, ExecutionStatus.COMPLETED);
            
            return result;
            
        } catch (Exception e) {
            ExecutionResult result = ExecutionResult.failed(
                executionId, 
                workflow.id().asString(), 
                e.getMessage()
            );
            results.put(executionId, result);
            statuses.put(executionId, ExecutionStatus.FAILED);
            throw e;
        }
    }
    
    @Override
    public ExecutionResult executeSkill(SkillDefinition skill, ExecutionContext context) 
            throws Exception {
        String executionId = context.id().asString();
        statuses.put(executionId, ExecutionStatus.RUNNING);
        
        try {
            // Validate skill
            if (skill == null) {
                throw new IllegalArgumentException("Skill definition is required");
            }
            
            // Execute skill
            List<Artifact> outputs = new ArrayList<>();
            
            // Simulate execution
            Thread.sleep(50);
            
            ExecutionResult result = ExecutionResult.success(
                executionId, 
                skill.id().asString(), 
                "skill",
                outputs
            );
            
            results.put(executionId, result);
            statuses.put(executionId, ExecutionStatus.COMPLETED);
            
            return result;
            
        } catch (Exception e) {
            ExecutionResult result = ExecutionResult.failed(
                executionId, 
                skill.id().asString(), 
                e.getMessage()
            );
            results.put(executionId, result);
            statuses.put(executionId, ExecutionStatus.FAILED);
            throw e;
        }
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeAsync(AgentDefinition agent, 
            ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeAgent(agent, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    @Override
    public void pause(String executionId) throws Exception {
        Future<?> future = runningExecutions.get(executionId);
        if (future != null) {
            future.cancel(true);
        }
        statuses.put(executionId, ExecutionStatus.PAUSED);
    }
    
    @Override
    public void resume(String executionId) throws Exception {
        statuses.put(executionId, ExecutionStatus.RUNNING);
        // In practice, resume logic would be more complex
    }
    
    @Override
    public void cancel(String executionId) throws Exception {
        Future<?> future = runningExecutions.get(executionId);
        if (future != null) {
            future.cancel(true);
        }
        statuses.put(executionId, ExecutionStatus.CANCELLED);
    }
    
    @Override
    public ExecutionStatus getStatus(String executionId) throws Exception {
        return statuses.getOrDefault(executionId, ExecutionStatus.UNKNOWN);
    }
    
    @Override
    public Optional<ExecutionResult> getResult(String executionId) throws Exception {
        return Optional.ofNullable(results.get(executionId));
    }
    
    private ExecutionStepResult executeWorkflowStep(WorkflowStep step, ExecutionContext context) {
        try {
            // Simulate step execution
            Thread.sleep(50);
            return ExecutionStepResult.success(
                step.id(),
                step.name(),
                "step"
            );
        } catch (Exception e) {
            return ExecutionStepResult.failed(
                step.id(),
                step.name(),
                "step",
                e.getMessage()
            );
        }
    }
    
    @Override
    public void initialize() throws Exception {
        // Initialize execution engine
    }
    
    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
