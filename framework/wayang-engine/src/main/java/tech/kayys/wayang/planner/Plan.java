package tech.kayys.wayang.planner;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;


/**
 * Plan - complete plan model
 */
public record Plan(
    String id,
    String name,
    String description,
    List<PlanStep> steps,
    Map<String, Object> variables,
    Map<String, Object> metadata,
    PlanningStrategy strategy,
    double confidence,
    Instant createdAt,
    Instant updatedAt
) {
    public static PlanBuilder builder() {
        return new PlanBuilder();
    }
    
    public static Plan of(String name, List<PlanStep> steps) {
        return new Plan(
            Id.random().asString(),
            name,
            null,
            steps,
            Map.of(),
            Map.of(),
            PlanningStrategy.SEQUENTIAL,
            1.0,
            Instant.now(),
            Instant.now()
        );
    }
    
    public Plan withVariable(String key, Object value) {
        Map<String, Object> newVariables = new HashMap<>(variables);
        newVariables.put(key, value);
        return new Plan(id, name, description, steps, newVariables, metadata, 
            strategy, confidence, createdAt, Instant.now());
    }
    
    public Plan withStep(PlanStep step) {
        List<PlanStep> newSteps = new ArrayList<>(steps);
        newSteps.add(step);
        return new Plan(id, name, description, newSteps, variables, metadata, 
            strategy, confidence, createdAt, Instant.now());
    }
    
    public Plan withConfidence(double confidence) {
        return new Plan(id, name, description, steps, variables, metadata, 
            strategy, confidence, createdAt, Instant.now());
    }
    
    public static class PlanBuilder {
        private String id;
        private String name;
        private String description;
        private final List<PlanStep> steps = new ArrayList<>();
        private final Map<String, Object> variables = new HashMap<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private PlanningStrategy strategy = PlanningStrategy.SEQUENTIAL;
        private double confidence = 1.0;
        private Instant createdAt;
        private Instant updatedAt;
        
        public PlanBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public PlanBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public PlanBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public PlanBuilder step(PlanStep step) {
            this.steps.add(step);
            return this;
        }
        
        public PlanBuilder steps(PlanStep... steps) {
            this.steps.addAll(List.of(steps));
            return this;
        }
        
        public PlanBuilder variable(String key, Object value) {
            this.variables.put(key, value);
            return this;
        }
        
        public PlanBuilder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public PlanBuilder strategy(PlanningStrategy strategy) {
            this.strategy = strategy;
            return this;
        }
        
        public PlanBuilder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }
        
        public PlanBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public PlanBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Plan build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            if (updatedAt == null) {
                updatedAt = createdAt;
            }
            return new Plan(id, name, description, steps, variables, metadata, 
                strategy, confidence, createdAt, updatedAt);
        }
    }
}