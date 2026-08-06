package tech.kayys.wayang.reasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.planner.Plan;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultReasoner implements Reasoner {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    
    public DefaultReasoner() {
        this.id = Id.random().asString();
        this.name = "default-reasoner";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Reasoner")
            .version(version)
            .label("type", "reasoner")
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
    public ResourceType type() { return new ResourceType.Custom("reasoner"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public ReasoningResult reason(Plan plan, ExecutionContext context) throws Exception {
        // Get goal from context
        String goal = context.getVariable("goal", String.class);
        if (goal == null) {
            goal = context.getVariable("query", String.class);
        }
        
        // Build reasoning steps
        List<ReasoningStep> steps = new ArrayList<>();
        
        // Step 1: Understand the goal
        steps.add(ReasoningStep.of(
            "understanding",
            "Understanding the goal",
            "The user wants to: " + (goal != null ? goal : "Unknown")
        ));
        
        // Step 2: Analyze available information
        ContextData contextData = context.getVariable("contextData", ContextData.class);
        if (contextData != null && !contextData.documents().isEmpty()) {
            steps.add(ReasoningStep.of(
                "analysis",
                "Analyzing available information",
                "Found " + contextData.documents().size() + " documents"
            ));
        }
        
        // Step 3: Generate conclusion
        String conclusion = "Based on the available information, I can help with: " + 
            (goal != null ? goal : "Unknown request");
        steps.add(ReasoningStep.of(
            "conclusion",
            "Conclusion",
            conclusion
        ));
        
        return ReasoningResult.of(conclusion, steps);
    }
    
    @Override
    public Set<ReasoningStrategy> getSupportedStrategies() {
        return Set.of(ReasoningStrategy.CHAIN_OF_THOUGHT, ReasoningStrategy.REACT);
    }
}
