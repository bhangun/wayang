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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Reasoning Step
 */
public record ReasoningStep(
    String id,
    String type,
    String description,
    String content,
    Map<String, Object> metadata,
    List<ReasoningStep> substeps,
    int order,
    double confidence
) {
    public static ReasoningStep of(String type, String description, String content) {
        return new ReasoningStep(
            Id.random().asString(),
            type,
            description,
            content,
            Map.of(),
            List.of(),
            0,
            1.0
        );
    }
    
    public ReasoningStep withSubstep(ReasoningStep substep) {
        List<ReasoningStep> newSubsteps = new ArrayList<>(substeps);
        newSubsteps.add(substep);
        return new ReasoningStep(id, type, description, content, metadata, 
            newSubsteps, order, confidence);
    }
}