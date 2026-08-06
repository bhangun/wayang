package tech.kayys.wayang.event.domain;
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

import tech.kayys.wayang.definition.WorkflowDefinition;
import tech.kayys.wayang.event.EventPayload;
import tech.kayys.wayang.resource.Artifact;

/**
 * Workflow Events
 */
public interface WorkflowEvent extends EventPayload {
    
    record WorkflowStarted(
        WorkflowDefinition workflow,
        String executionId,
        Map<String, Object> inputs
    ) implements WorkflowEvent {}
    
    record WorkflowCompleted(
        String executionId,
        List<Artifact> outputs,
        long durationMs
    ) implements WorkflowEvent {}
    
    record WorkflowFailed(
        String executionId,
        String error
    ) implements WorkflowEvent {}
    
    record WorkflowStepStarted(
        String executionId,
        String stepId,
        String stepName
    ) implements WorkflowEvent {}
    
    record WorkflowStepCompleted(
        String executionId,
        String stepId,
        String stepName,
        List<Artifact> outputs
    ) implements WorkflowEvent {}
    
    record WorkflowStepFailed(
        String executionId,
        String stepId,
        String stepName,
        String error
    ) implements WorkflowEvent {}
    
    record WorkflowTransition(
        String executionId,
        String from,
        String to,
        String condition
    ) implements WorkflowEvent {}
}