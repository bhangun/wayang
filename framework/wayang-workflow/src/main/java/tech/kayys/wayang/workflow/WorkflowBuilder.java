package tech.kayys.wayang.workflow;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Reference;
import tech.kayys.wayang.definition.WorkflowDefinition;
import tech.kayys.wayang.definition.WorkflowStep;
import tech.kayys.wayang.definition.WorkflowTransition;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Workflow Builder
 */
public class WorkflowBuilder {
    private String name;
    private String description;
    private String version = "1.0.0";
    private final List<WorkflowStep> steps = new ArrayList<>();
    private final Map<String, WorkflowTransition> transitions = new HashMap<>();
    private boolean parallel = false;
    private int maxConcurrency = 10;
    private String executionStrategy = "sequential";
    
    public WorkflowBuilder named(String name) {
        this.name = name;
        return this;
    }
    
    public WorkflowBuilder describedAs(String description) {
        this.description = description;
        return this;
    }
    
    public WorkflowBuilder version(String version) {
        this.version = version;
        return this;
    }
    
    public WorkflowBuilder step(String id, String name, String action) {
        this.steps.add(WorkflowStep.of(id, name, Reference.of(Id.random(), new ResourceType.Custom("step"), action)));
        return this;
    }
    
    public WorkflowBuilder step(String id, String name, String action, Map<String, Object> parameters) {
        this.steps.add(WorkflowStep.of(id, name, Reference.of(Id.random(), new ResourceType.Custom("step"), action), parameters));
        return this;
    }
    
    public WorkflowBuilder transition(String from, String to) {
        return transition(from, to, null);
    }
    
    public WorkflowBuilder transition(String from, String to, String condition) {
        this.transitions.put(from + "->" + to, new WorkflowTransition(from, to, condition));
        return this;
    }
    
    public WorkflowBuilder parallel() {
        this.parallel = true;
        return this;
    }
    
    public WorkflowBuilder maxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        return this;
    }
    
    public WorkflowBuilder executionStrategy(String strategy) {
        this.executionStrategy = strategy;
        return this;
    }
    
    public WorkflowDefinition build() {
        WorkflowDefinition.Builder builder = WorkflowDefinition.builder()
            .metadata(Metadata.builder()
                .name(name)
                .description(description)
                .version(version)
                .now()
                .build())
            .executionStrategy(executionStrategy)
            .parallel(parallel)
            .maxConcurrency(maxConcurrency);
        
        for (WorkflowStep step : steps) {
            builder.step(step);
        }
        
        for (WorkflowTransition transition : transitions.values()) {
            builder.transition(transition.from(), transition.to(), transition.condition());
        }
        
        return builder.build();
    }
}