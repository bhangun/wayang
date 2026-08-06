package tech.kayys.wayang.reasoner;
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
import java.util.Map;

import tech.kayys.wayang.extension.Id;


/**
 * Reasoning Result - complete reasoning result model
 */
public record ReasoningResult(
    String id,
    String conclusion,
    List<ReasoningStep> steps,
    List<String> alternatives,
    Map<String, Object> evidence,
    Map<String, Object> metadata,
    double confidence,
    ReasoningStrategy strategy,
    long reasoningTimeMs,
    Instant timestamp
) {
    public static ReasoningResult of(String conclusion) {
        return new ReasoningResult(
            Id.random().asString(),
            conclusion,
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            1.0,
            ReasoningStrategy.CHAIN_OF_THOUGHT,
            0,
            Instant.now()
        );
    }
    
    public static ReasoningResult of(String conclusion, List<ReasoningStep> steps) {
        return new ReasoningResult(
            Id.random().asString(),
            conclusion,
            steps,
            List.of(),
            Map.of(),
            Map.of(),
            1.0,
            ReasoningStrategy.CHAIN_OF_THOUGHT,
            0,
            Instant.now()
        );
    }
    
    public ReasoningResult withConfidence(double confidence) {
        return new ReasoningResult(id, conclusion, steps, alternatives, evidence, 
            metadata, confidence, strategy, reasoningTimeMs, timestamp);
    }
    
    public ReasoningResult withAlternative(String alternative) {
        List<String> newAlternatives = new ArrayList<>(alternatives);
        newAlternatives.add(alternative);
        return new ReasoningResult(id, conclusion, steps, newAlternatives, evidence, 
            metadata, confidence, strategy, reasoningTimeMs, timestamp);
    }
}