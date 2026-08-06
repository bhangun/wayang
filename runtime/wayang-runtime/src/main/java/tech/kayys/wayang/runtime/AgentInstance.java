package tech.kayys.wayang.runtime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.State;
import tech.kayys.wayang.definition.AgentDefinition;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionError;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.identity.ResourceId.ExecutionId;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Agent Instance - a running agent
 */
public final class AgentInstance extends BaseResource implements RuntimeInstance {
    
    private final AgentDefinition definition;
    private final ExecutionId executionId;
    private final State currentState;
    private final Instant startedAt;
    private final Instant updatedAt;
    private final ExecutionContext context;
    private final List<Artifact> outputs;
    private final List<ExecutionError> errors;
    
    private AgentInstance(Builder builder) {
        super(
            new ResourceId.InstanceId(Id.random()),
            Metadata.builder()
                .name(builder.definition != null ? builder.definition.metadata().name() : "agent-instance")
                .label("type", "agent-instance")
                .label("definition", builder.definition != null ? builder.definition.id().asString() : null)
                .now()
                .build()
        );
        this.definition = builder.definition;
        this.executionId = builder.executionId != null ? builder.executionId : new ExecutionId(Id.random());
        this.currentState = builder.currentState != null ? builder.currentState : State.INITIAL;
        this.startedAt = builder.startedAt != null ? builder.startedAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : this.startedAt;
        this.context = builder.context != null ? builder.context : ExecutionContext.builder().build();
        this.outputs = builder.outputs != null ? List.copyOf(builder.outputs) : List.of();
        this.errors = builder.errors != null ? List.copyOf(builder.errors) : List.of();
    }
    
    public AgentDefinition definition() { return definition; }
    
    @Override
    public ResourceId definitionId() {
        return definition != null ? definition.id() : new ResourceId.CustomId(Id.random(), new ResourceType.Custom("unknown"));
    }
    
    @Override
    public ExecutionId executionId() { return executionId; }
    
    @Override
    public State currentState() { return currentState; }
    
    @Override
    public Instant startedAt() { return startedAt; }
    
    @Override
    public Instant updatedAt() { return updatedAt; }
    
    @Override
    public ExecutionContext context() { return context; }
    
    @Override
    public List<Artifact> outputs() { return outputs; }
    
    @Override
    public List<ExecutionError> errors() { return errors; }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Instance();
    }
    
    public AgentInstance withState(State state) {
        return new Builder(this)
            .currentState(state)
            .updatedAt(Instant.now())
            .build();
    }
    
    public AgentInstance withContext(ExecutionContext context) {
        return new Builder(this)
            .context(context)
            .updatedAt(Instant.now())
            .build();
    }
    
    public AgentInstance withOutputs(List<Artifact> outputs) {
        List<Artifact> newOutputs = new ArrayList<>(this.outputs);
        newOutputs.addAll(outputs);
        return new Builder(this)
            .outputs(newOutputs)
            .updatedAt(Instant.now())
            .build();
    }
    
    public AgentInstance withError(ExecutionError error) {
        List<ExecutionError> newErrors = new ArrayList<>(errors);
        newErrors.add(error);
        return new Builder(this)
            .errors(newErrors)
            .currentState(State.FAILED)
            .updatedAt(Instant.now())
            .build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private AgentDefinition definition;
        private ExecutionId executionId;
        private State currentState;
        private Instant startedAt;
        private Instant updatedAt;
        private ExecutionContext context;
        private List<Artifact> outputs;
        private List<ExecutionError> errors;
        
        public Builder() {}
        
        public Builder(AgentInstance instance) {
            this.definition = instance.definition;
            this.executionId = instance.executionId;
            this.currentState = instance.currentState;
            this.startedAt = instance.startedAt;
            this.updatedAt = instance.updatedAt;
            this.context = instance.context;
            this.outputs = new ArrayList<>(instance.outputs);
            this.errors = new ArrayList<>(instance.errors);
        }
        
        public Builder definition(AgentDefinition definition) {
            this.definition = definition;
            return this;
        }
        
        public Builder executionId(ExecutionId executionId) {
            this.executionId = executionId;
            return this;
        }
        
        public Builder currentState(State currentState) {
            this.currentState = currentState;
            return this;
        }
        
        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder context(ExecutionContext context) {
            this.context = context;
            return this;
        }
        
        public Builder outputs(List<Artifact> outputs) {
            this.outputs = outputs;
            return this;
        }
        
        public Builder errors(List<ExecutionError> errors) {
            this.errors = errors;
            return this;
        }
        
        public AgentInstance build() {
            return new AgentInstance(this);
        }
    }
}