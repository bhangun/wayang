package tech.kayys.wayang.workflow.api.model;

import java.time.Instant;
import java.util.Map;

/**
 * Workflow Event (immutable)
 */
public record WorkflowEvent(
        String id,
        String runId,
        Long sequence,
        WorkflowEventType type,
        Map<String, Object> data,
        Instant timestamp) {

    public static WorkflowEvent created(String runId, Map<String, Object> data) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.CREATED, data, Instant.now());
    }

    public static WorkflowEvent statusChanged(String runId, tech.kayys.wayang.workflow.api.model.RunStatus oldStatus,
            tech.kayys.wayang.workflow.api.model.RunStatus newStatus) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.STATUS_CHANGED,
                Map.of("oldStatus", oldStatus, "newStatus", newStatus), Instant.now());
    }

    public static WorkflowEvent nodeExecuted(String runId, String nodeId, String status) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.NODE_EXECUTED,
                Map.of("nodeId", nodeId, "status", status), Instant.now());
    }

    public static WorkflowEvent completed(String runId, Map<String, Object> outputs) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.COMPLETED, outputs, Instant.now());
    }

    public static WorkflowEvent stateUpdated(String runId, Map<String, Object> variables) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.STATE_UPDATED, variables, Instant.now());
    }

    public static WorkflowEvent resumed(String runId, String nodeId, Map<String, Object> data, String step) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.RESUMED,
                Map.of("nodeId", nodeId, "data", data, "step", step), Instant.now());
    }

    public static WorkflowEvent cancelled(String runId, String reason, String userId) {
        return new WorkflowEvent(null, runId, null, WorkflowEventType.CANCELLED,
                Map.of("reason", reason, "userId", userId), Instant.now());
    }
}
