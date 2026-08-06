package tech.kayys.wayang.evaluator;
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
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.execution.ExecutionContext;


/**
 * Evaluator - evaluates execution results.
 */
public interface Evaluator extends Extension {
    
    /**
     * Evaluate execution context
     */
    Evaluation evaluate(ExecutionContext context) throws Exception;
    
    /**
     * Evaluate with outputs
     */
    default Evaluation evaluate(ExecutionContext context, List<Artifact> outputs) throws Exception {
        return evaluate(context);
    }
    
    /**
     * Evaluate asynchronously
     */
    default CompletableFuture<Evaluation> evaluateAsync(ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return evaluate(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Get evaluation metrics
     */
    default Set<String> getMetrics() {
        return Set.of("accuracy", "relevance", "completeness");
    }
}