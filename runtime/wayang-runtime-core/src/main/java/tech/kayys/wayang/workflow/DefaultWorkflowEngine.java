package tech.kayys.wayang.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.definition.WorkflowDefinition;
import tech.kayys.wayang.definition.WorkflowStep;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultWorkflowEngine implements WorkflowEngine {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, WorkflowStatus> statuses = new ConcurrentHashMap<>();
    private final Map<String, WorkflowResult> results = new ConcurrentHashMap<>();
    
    public DefaultWorkflowEngine() {
        this.id = Id.random().asString();
        this.name = "default-workflow-engine";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Workflow Engine")
            .version(version)
            .label("type", "workflow")
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
    public ResourceType type() { return new ResourceType.Custom("workflow"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public WorkflowResult execute(WorkflowDefinition workflow, ExecutionContext context) 
            throws Exception {
        String executionId = context.id().asString();
        statuses.put(executionId, WorkflowStatus.RUNNING);
        long startTime = System.currentTimeMillis();
        
        List<Artifact> outputs = new ArrayList<>();
        List<WorkflowStepResult> stepResults = new ArrayList<>();
        
        try {
            for (WorkflowStep step : workflow.steps()) {
                // Simulate step execution
                Thread.sleep(50);
                stepResults.add(WorkflowStepResult.success(step.id(), step.name()));
            }
            
            long endTime = System.currentTimeMillis();
            statuses.put(executionId, WorkflowStatus.COMPLETED);
            
            WorkflowResult result = WorkflowResult.success(
                workflow.id().asString(),
                outputs
            );
            
            for (WorkflowStepResult stepResult : stepResults) {
                result = result.withStepResult(stepResult);
            }
            
            results.put(executionId, result);
            return result;
            
        } catch (Exception e) {
            statuses.put(executionId, WorkflowStatus.FAILED);
            return WorkflowResult.failed(workflow.id().asString(), e.getMessage());
        }
    }
    
    @Override
    public void pause(String executionId) throws Exception {
        statuses.put(executionId, WorkflowStatus.PAUSED);
    }
    
    @Override
    public void resume(String executionId) throws Exception {
        statuses.put(executionId, WorkflowStatus.RUNNING);
    }
    
    @Override
    public void cancel(String executionId) throws Exception {
        statuses.put(executionId, WorkflowStatus.CANCELLED);
    }
    
    @Override
    public WorkflowStatus getStatus(String executionId) throws Exception {
        return statuses.getOrDefault(executionId, WorkflowStatus.UNKNOWN);
    }
}