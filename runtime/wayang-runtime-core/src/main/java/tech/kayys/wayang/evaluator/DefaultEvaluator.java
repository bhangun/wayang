package tech.kayys.wayang.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionState;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.inference.CompletionResult;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultEvaluator implements Evaluator {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    
    public DefaultEvaluator() {
        this.id = Id.random().asString();
        this.name = "default-evaluator";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Evaluator")
            .version(version)
            .label("type", "evaluator")
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
    public ResourceType type() { return new ResourceType.Custom("evaluator"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public Evaluation evaluate(ExecutionContext context) throws Exception {
        double score = 0.85;
        EvaluationStatus status = EvaluationStatus.PASSED;
        List<EvaluationIssue> issues = new ArrayList<>();
        Map<String, Double> metrics = new HashMap<>();
        
        // Check if there was an error
        if (context.state() == ExecutionState.FAILED) {
            score = 0.0;
            status = EvaluationStatus.FAILED;
            issues.add(EvaluationIssue.of("execution_failed", "Execution failed", "ERROR"));
        }
        
        // Check if there are outputs
        if (!context.artifacts().isEmpty()) {
            metrics.put("has_outputs", 1.0);
        }
        
        // Check if there's a completion result
        CompletionResult completion = context.getVariable("completionResult", CompletionResult.class);
        if (completion != null && completion.getContent() != null) {
            metrics.put("has_response", 1.0);
        }
        
        return Evaluation.of(score, status)
            .withMetric("accuracy", score)
            .withMetric("relevance", 0.8)
            .withMetric("completeness", 0.7);
    }
    
    @Override
    public Set<String> getMetrics() {
        return Set.of("accuracy", "relevance", "completeness", "has_outputs", "has_response");
    }
}