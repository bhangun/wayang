package tech.kayys.wayang.workflow;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.*;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Reference;
import tech.kayys.wayang.descriptor.Descriptor;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.definition.Definition;

/**
 * Workflow definition - describes how work is organized and executed.
 */
public final class WorkflowDefinition extends Definition {
    
    private final List<WorkflowStep> steps;
    private final Map<String, WorkflowTransition> transitions;
    private final String executionStrategy;
    private final boolean parallel;
    private final int maxConcurrency;
    private final WorkflowDescriptor descriptor;
    
    private WorkflowDefinition(Builder builder) {
        super(
            new ResourceId.WorkflowId(Id.random()),
            builder.metadata,
            builder.dependencies,
            builder.configuration
        );
        this.steps = builder.steps != null ? List.copyOf(builder.steps) : List.of();
        this.transitions = builder.transitions != null ? Map.copyOf(builder.transitions) : Map.of();
        this.executionStrategy = builder.executionStrategy != null ? builder.executionStrategy : "sequential";
        this.parallel = builder.parallel;
        this.maxConcurrency = builder.maxConcurrency > 0 ? builder.maxConcurrency : 10;
        this.descriptor = new WorkflowDescriptor(
            id().asString(),
            metadata().name(),
            metadata().version().toString(),
            metadata().description(),
            Set.of("workflow"),
            Set.of("workflow"),
            Map.of(),
            Map.of(),
            List.of(),
            List.of()
        );
    }
    
    public List<WorkflowStep> steps() { return steps; }
    public Map<String, WorkflowTransition> transitions() { return transitions; }
    public String executionStrategy() { return executionStrategy; }
    public boolean parallel() { return parallel; }
    public int maxConcurrency() { return maxConcurrency; }
    
    @Override
    public Descriptor descriptor() {
        return descriptor;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Metadata metadata = Metadata.empty();
        private List<WorkflowStep> steps;
        private Map<String, WorkflowTransition> transitions;
        private String executionStrategy;
        private boolean parallel;
        private int maxConcurrency;
        private final Set<Reference> dependencies = new HashSet<>();
        private final Map<String, Object> configuration = new HashMap<>();
        
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder step(WorkflowStep step) {
            if (steps == null) {
                steps = new ArrayList<>();
            }
            steps.add(step);
            if (step.ref() != null) {
                dependencies.add(step.ref());
            }
            return this;
        }
        
        public Builder steps(WorkflowStep... steps) {
            this.steps = List.of(steps);
            for (WorkflowStep step : steps) {
                if (step.ref() != null) {
                    dependencies.add(step.ref());
                }
            }
            return this;
        }
        
        public Builder transition(String from, String to, String condition) {
            if (transitions == null) {
                transitions = new HashMap<>();
            }
            transitions.put(from + "->" + to, new WorkflowTransition(from, to, condition));
            return this;
        }
        
        public Builder transitions(Map<String, WorkflowTransition> transitions) {
            this.transitions = transitions;
            return this;
        }
        
        public Builder executionStrategy(String executionStrategy) {
            this.executionStrategy = executionStrategy;
            return this;
        }
        
        public Builder parallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }
        
        public Builder maxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
            return this;
        }
        
        public Builder configuration(Map<String, Object> configuration) {
            this.configuration.putAll(configuration);
            return this;
        }
        
        public WorkflowDefinition build() {
            return new WorkflowDefinition(this);
        }
    }
}