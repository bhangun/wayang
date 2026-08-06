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


import java.util.*;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.execution.ExecutionContext;


/**
 * Planner - creates execution plans.
 */
public interface Planner extends Extension {
    
    /**
     * Create a plan
     */
    Plan createPlan(ExecutionContext context) throws Exception;
    
    /**
     * Create a plan with context data
     */
    default Plan createPlan(ExecutionContext context, ContextData contextData) throws Exception {
        return createPlan(context);
    }
    
    /**
     * Create a plan asynchronously
     */
    default CompletableFuture<Plan> createPlanAsync(ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return createPlan(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get supported planning strategies
     */
    default Set<PlanningStrategy> getSupportedStrategies() {
        return Set.of(PlanningStrategy.SEQUENTIAL);
    }
    
    /**
     * Validate a plan
     */
    default boolean validate(Plan plan) {
        return plan != null && !plan.steps().isEmpty();
    }
}