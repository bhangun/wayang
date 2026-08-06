package tech.kayys.wayang.definition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.*;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Reference;
import tech.kayys.wayang.descriptor.Descriptor;
import tech.kayys.wayang.identity.ResourceId;

/**
 * Agent definition - describes what an agent is.
 */
public final class AgentDefinition extends Definition {
    
    private final String role;
    private final String goal;
    private final List<Reference> skills;
    private final List<Reference> tools;
    private final Reference workflow;
    private final Reference memory;
    private final Reference knowledge;
    private final Reference planner;
    private final Reference reasoner;
    private final Reference model;
    private final Map<String, Object> constraints;
    private final AgentDefinition descriptor;
    
    private AgentDefinition(Builder builder) {
        super(
            new ResourceId.AgentId(Id.random()),
            builder.metadata,
            builder.dependencies,
            builder.configuration
        );
        this.role = builder.role;
        this.goal = builder.goal;
        this.skills = builder.skills != null ? List.copyOf(builder.skills) : List.of();
        this.tools = builder.tools != null ? List.copyOf(builder.tools) : List.of();
        this.workflow = builder.workflow;
        this.memory = builder.memory;
        this.knowledge = builder.knowledge;
        this.planner = builder.planner;
        this.reasoner = builder.reasoner;
        this.model = builder.model;
        this.constraints = builder.constraints != null ? Map.copyOf(builder.constraints) : Map.of();
        this.descriptor = this;
    }
    
    public String role() { return role; }
    public String goal() { return goal; }
    public List<Reference> skills() { return skills; }
    public List<Reference> tools() { return tools; }
    public Reference workflow() { return workflow; }
    public Reference memory() { return memory; }
    public Reference knowledge() { return knowledge; }
    public Reference planner() { return planner; }
    public Reference reasoner() { return reasoner; }
    public Reference model() { return model; }
    public Map<String, Object> constraints() { return constraints; }
    
    @Override
    public Descriptor descriptor() {
        return new AgentDescriptor(
            id().asString(),
            metadata().name(),
            metadata().version().toString(),
            metadata().description(),
            Set.of(role != null ? role : "agent"),
            Set.of("agent"),
            Map.of(),
            Map.of(),
            List.of(),
            List.of()
        );
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Metadata metadata = Metadata.empty();
        private String role;
        private String goal;
        private List<Reference> skills;
        private List<Reference> tools;
        private Reference workflow;
        private Reference memory;
        private Reference knowledge;
        private Reference planner;
        private Reference reasoner;
        private Reference model;
        private Map<String, Object> constraints;
        private final Set<Reference> dependencies = new HashSet<>();
        private final Map<String, Object> configuration = new HashMap<>();
        
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder role(String role) {
            this.role = role;
            return this;
        }
        
        public Builder goal(String goal) {
            this.goal = goal;
            return this;
        }
        
        public Builder skills(Reference... skills) {
            this.skills = List.of(skills);
            this.dependencies.addAll(List.of(skills));
            return this;
        }
        
        public Builder skills(List<Reference> skills) {
            this.skills = skills;
            this.dependencies.addAll(skills);
            return this;
        }
        
        public Builder tools(Reference... tools) {
            this.tools = List.of(tools);
            this.dependencies.addAll(List.of(tools));
            return this;
        }
        
        public Builder tools(List<Reference> tools) {
            this.tools = tools;
            this.dependencies.addAll(tools);
            return this;
        }
        
        public Builder workflow(Reference workflow) {
            this.workflow = workflow;
            if (workflow != null) this.dependencies.add(workflow);
            return this;
        }
        
        public Builder memory(Reference memory) {
            this.memory = memory;
            if (memory != null) this.dependencies.add(memory);
            return this;
        }
        
        public Builder knowledge(Reference knowledge) {
            this.knowledge = knowledge;
            if (knowledge != null) this.dependencies.add(knowledge);
            return this;
        }
        
        public Builder planner(Reference planner) {
            this.planner = planner;
            if (planner != null) this.dependencies.add(planner);
            return this;
        }
        
        public Builder reasoner(Reference reasoner) {
            this.reasoner = reasoner;
            if (reasoner != null) this.dependencies.add(reasoner);
            return this;
        }
        
        public Builder model(Reference model) {
            this.model = model;
            if (model != null) this.dependencies.add(model);
            return this;
        }
        
        public Builder constraint(String key, Object value) {
            if (constraints == null) {
                constraints = new HashMap<>();
            }
            constraints.put(key, value);
            return this;
        }
        
        public Builder configuration(Map<String, Object> configuration) {
            this.configuration.putAll(configuration);
            return this;
        }
        
        public AgentDefinition build() {
            return new AgentDefinition(this);
        }
    }
}
