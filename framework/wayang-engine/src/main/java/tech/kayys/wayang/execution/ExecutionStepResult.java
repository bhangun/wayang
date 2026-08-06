package tech.kayys.wayang.execution;
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

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.Artifact;

/**
 * Execution Step Result
 */
public record ExecutionStepResult(
    String id,
    String stepId,
    String stepName,
    String stepType,
    ExecutionStatus status,
    List<Artifact> outputs,
    long durationMs,
    String error,
    Map<String, Object> metadata
) {
    public static ExecutionStepResult success(String stepId, String stepName, String stepType) {
        return new ExecutionStepResult(
            Id.random().asString(),
            stepId,
            stepName,
            stepType,
            ExecutionStatus.COMPLETED,
            List.of(),
            0,
            null,
            Map.of()
        );
    }
    
    public static ExecutionStepResult failed(String stepId, String stepName, String stepType, 
            String error) {
        return new ExecutionStepResult(
            Id.random().asString(),
            stepId,
            stepName,
            stepType,
            ExecutionStatus.FAILED,
            List.of(),
            0,
            error,
            Map.of()
        );
    }
}
