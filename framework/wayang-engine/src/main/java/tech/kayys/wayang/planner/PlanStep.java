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


import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Reference;



/**
 * Plan Step - complete plan step model
 */
public record PlanStep(
    String id,
    String name,
    String type,
    String description,
    Reference action,
    Map<String, Object> parameters,
    Map<String, Object> metadata,
    List<PlanStep> substeps,
    Condition condition,
    int order,
    boolean parallel
) {
    public static PlanStep of(String id, String name, String type, Reference action) {
        return new PlanStep(id, name, type, null, action, Map.of(), Map.of(), 
            List.of(), null, 0, false);
    }
    
    public static PlanStep of(String id, String name, String type, Reference action, Map<String, Object> parameters) {
        return new PlanStep(id, name, type, null, action, parameters, Map.of(), 
            List.of(), null, 0, false);
    }
    
    public PlanStep withSubstep(PlanStep substep) {
        List<PlanStep> newSubsteps = new ArrayList<>(substeps);
        newSubsteps.add(substep);
        return new PlanStep(id, name, type, description, action, parameters, 
            metadata, newSubsteps, condition, order, parallel);
    }
    
    public PlanStep withCondition(Condition condition) {
        return new PlanStep(id, name, type, description, action, parameters, 
            metadata, substeps, condition, order, parallel);
    }
}