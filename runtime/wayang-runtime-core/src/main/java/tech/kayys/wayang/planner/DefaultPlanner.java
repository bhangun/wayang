package tech.kayys.wayang.planner;

import tech.kayys.wayang.context.ContextData;

public class DefaultPlanner implements Planner {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    
    public DefaultPlanner() {
        this.id = Id.random().asString();
        this.name = "default-planner";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Planner")
            .version(version)
            .label("type", "planner")
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
    public ResourceType type() { return new ResourceType.Custom("planner"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public Plan createPlan(ExecutionContext context) throws Exception {
        // Get the goal from context
        String goal = context.getVariable("goal", String.class);
        if (goal == null) {
            goal = context.getVariable("query", String.class);
        }
        
        // Get context data
        ContextData contextData = context.getVariable("contextData", ContextData.class);
        
        // Build a simple plan
        PlanBuilder builder = Plan.builder()
            .name("Plan for: " + (goal != null ? goal : "Unknown goal"))
            .strategy(PlanningStrategy.SEQUENTIAL)
            .confidence(1.0);
        
        // Add steps based on context
        if (contextData != null && !contextData.documents().isEmpty()) {
            builder.step(PlanStep.of(
                "step1",
                "Retrieve Knowledge",
                "knowledge",
                Reference.of(Id.random(), new ResourceType.Custom("knowledge"), "retrieve"),
                Map.of("documents", contextData.documents())
            ));
        }
        
        // Add a reasoning step
        builder.step(PlanStep.of(
            "step2",
            "Reason",
            "reasoning",
            Reference.of(Id.random(), new ResourceType.Custom("reasoning"), "reason"),
            Map.of("goal", goal != null ? goal : "Unknown")
        ));
        
        // Add a response step
        builder.step(PlanStep.of(
            "step3",
            "Generate Response",
            "response",
            Reference.of(Id.random(), new ResourceType.Custom("response"), "generate"),
            Map.of()
        ));
        
        return builder.build();
    }
    
    @Override
    public Set<PlanningStrategy> getSupportedStrategies() {
        return Set.of(PlanningStrategy.SEQUENTIAL, PlanningStrategy.REACT);
    }
}