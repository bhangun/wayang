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


import java.util.Map;
import java.util.Objects;

import tech.kayys.wayang.extension.Reference;

/**
 * A step in a workflow.
 */
public record WorkflowStep(
    String id,
    String name,
    String description,
    Reference ref,
    Map<String, Object> parameters,
    Map<String, Object> properties
) {
    public WorkflowStep {
        Objects.requireNonNull(id, "id cannot be null");
    }
    
    public static WorkflowStep of(String id, String name, Reference ref) {
        return new WorkflowStep(id, name, null, ref, Map.of(), Map.of());
    }
    
    public static WorkflowStep of(String id, String name, Reference ref, Map<String, Object> parameters) {
        return new WorkflowStep(id, name, null, ref, parameters, Map.of());
    }
}