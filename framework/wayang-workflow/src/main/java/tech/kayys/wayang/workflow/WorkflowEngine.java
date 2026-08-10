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


import java.util.*;
import java.util.concurrent.CompletableFuture;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.extension.Metadata;


/**
 * Workflow Engine - executes workflows.
 */
public interface WorkflowEngine extends Extension {
    
    /**
     * Execute a workflow
     */
    WorkflowResult execute(WorkflowDefinition workflow, Map<String, Object> context) throws Exception;
    
    /**
     * Execute with inputs
     */
    default WorkflowResult execute(WorkflowDefinition workflow, Map<String, Object> context, 
            Map<String, Object> inputs) throws Exception {
        return execute(workflow, context);
    }
    
    /**
     * Execute asynchronously
     */
    default CompletableFuture<WorkflowResult> executeAsync(WorkflowDefinition workflow, 
            Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(workflow, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    

    /**
     * Pause a workflow
     */
    default void pause(String executionId) throws Exception {
        // Optional
    }
    
    /**
     * Resume a workflow
     */
    default void resume(String executionId) throws Exception {
        // Optional
    }
    
    /**
     * Cancel a workflow
     */
    default void cancel(String executionId) throws Exception {
        // Optional
    }
    
    /**
     * Get workflow status
     */
    default WorkflowStatus getStatus(String executionId) throws Exception {
        return WorkflowStatus.UNKNOWN;
    }
}