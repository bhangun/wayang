package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.gamelan.GamelanWorkflowRunManager;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsExecutionMutationSupport {
    private ProjectsExecutionMutationSupport() {
    }

    static Response stopExecution(
            String projectId,
            String executionId,
            String ifMatch,
            Map<String, Object> request,
            WayangDefinitionService definitionService,
            Set<String> supportedStopReasons,
            String statusUnknown,
            String statusStopped,
            String errorExecutionNotFound,
            String errorExecutionInvalidTransition,
            String errorExecutionVersionConflict,
            String errorExecutionStopFailed,
            String errorInvalidStopReason,
            long defaultRetryAfterSeconds) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            final Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return ProjectsExecutionLifecycleSupport.errorResponse(
                        Response.Status.NOT_FOUND,
                        errorExecutionNotFound,
                        "Execution not found: " + executionId,
                        false,
                        Map.of("executionId", executionId),
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final Long expectedVersion = ProjectsExecutionLifecycleSupport.resolveExpectedVersion(
                    ifMatch,
                    body.get("expectedVersion"));
            if (expectedVersion != null) {
                final Response versionConflict = ProjectsExecutionLifecycleSupport.validateExpectedVersion(
                        execution,
                        expectedVersion,
                        executionId,
                        errorExecutionVersionConflict,
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
                if (versionConflict != null) {
                    return versionConflict;
                }
            }
            final String stopReason = ProjectsExecutionLifecycleSupport.resolveStopReason(
                    body.get("reason"),
                    supportedStopReasons,
                    "USER_REQUEST");
            if (stopReason == null) {
                return ProjectsExecutionLifecycleSupport.errorResponse(
                        Response.Status.BAD_REQUEST,
                        errorInvalidStopReason,
                        "Unsupported stop reason",
                        false,
                        Map.of(
                                "supportedReasons", supportedStopReasons,
                                "providedReason", ProjectsValueSupport.optionalStringValue(body.get("reason"))),
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
            }
            final String stopNote = ProjectsValueSupport.optionalStringValue(body.get("note"));
            final String currentStatus = ProjectsExecutionStatusSupport.normalizeStatus(
                    execution.getOrDefault("status", statusUnknown),
                    statusUnknown);
            if (statusStopped.equals(currentStatus)) {
                return ProjectsExecutionLifecycleSupport.transitionError(
                        executionId,
                        currentStatus,
                        statusStopped,
                        errorExecutionInvalidTransition);
            }
            if (!ProjectsExecutionStatusSupport.isStatusTransitionAllowed(currentStatus, statusStopped, statusUnknown)) {
                return ProjectsExecutionLifecycleSupport.transitionError(
                        executionId,
                        currentStatus,
                        statusStopped,
                        errorExecutionInvalidTransition);
            }

            final boolean stopped = definitionService.stopExecution(executionId)
                    .await().indefinitely();
            if (!stopped) {
                return ProjectsExecutionLifecycleSupport.errorResponse(
                        Response.Status.CONFLICT,
                        errorExecutionStopFailed,
                        "Execution could not be stopped: " + executionId,
                        true,
                        Map.of("executionId", executionId, "reason", stopReason),
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
            }

            final String now = Instant.now().toString();
            execution.put("status", statusStopped);
            execution.put("stoppedAt", now);
            execution.put("stopReason", stopReason);
            if (stopNote != null) {
                execution.put("stopNote", stopNote);
            }
            execution.put("updatedAt", now);
            ProjectsExecutionLifecycleSupport.bumpExecutionVersion(execution);
            ProjectsFileStore.writeExecutions(executions);
            final Map<String, Object> stopMeta = new LinkedHashMap<>();
            stopMeta.put("reason", stopReason);
            if (stopNote != null) {
                stopMeta.put("note", stopNote);
            }
            ProjectsFileStore.appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_STOPPED",
                    statusStopped,
                    "Execution stop requested",
                    stopMeta);

            return Response.ok(execution).build();
        } catch (Exception e) {
            return ProjectsExecutionLifecycleSupport.errorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    errorExecutionStopFailed,
                    "Failed to stop execution",
                    true,
                    ProjectsExecutionLifecycleSupport.errorDetails(executionId, e),
                    ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
        }
    }

    static Response resumeExecution(
            String projectId,
            String executionId,
            String ifMatch,
            Map<String, Object> request,
            GamelanWorkflowRunManager workflowRunManager,
            String statusUnknown,
            String statusRunning,
            String errorExecutionNotFound,
            String errorExecutionInvalidTransition,
            String errorExecutionVersionConflict,
            String errorExecutionResumeFailed,
            long defaultRetryAfterSeconds) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            final Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return ProjectsExecutionLifecycleSupport.errorResponse(
                        Response.Status.NOT_FOUND,
                        errorExecutionNotFound,
                        "Execution not found: " + executionId,
                        false,
                        Map.of("executionId", executionId),
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
            }

            final String currentStatus = ProjectsExecutionStatusSupport.normalizeStatus(
                    execution.getOrDefault("status", statusUnknown),
                    statusUnknown);
            if (statusRunning.equals(currentStatus)) {
                return ProjectsExecutionLifecycleSupport.transitionError(
                        executionId,
                        currentStatus,
                        statusRunning,
                        errorExecutionInvalidTransition);
            }
            if (!ProjectsExecutionStatusSupport.isStatusTransitionAllowed(currentStatus, statusRunning, statusUnknown)) {
                return ProjectsExecutionLifecycleSupport.transitionError(
                        executionId,
                        currentStatus,
                        statusRunning,
                        errorExecutionInvalidTransition);
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final Long expectedVersion = ProjectsExecutionLifecycleSupport.resolveExpectedVersion(
                    ifMatch,
                    body.get("expectedVersion"));
            if (expectedVersion != null) {
                final Response versionConflict = ProjectsExecutionLifecycleSupport.validateExpectedVersion(
                        execution,
                        expectedVersion,
                        executionId,
                        errorExecutionVersionConflict,
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
                if (versionConflict != null) {
                    return versionConflict;
                }
            }
            final String humanTaskId = ProjectsValueSupport.optionalStringValue(body.get("humanTaskId"));
            final Map<String, Object> resumeData = ProjectsValueSupport.mapValue(body.get("data"));

            workflowRunManager.resumeRun(executionId, humanTaskId, resumeData)
                    .await().indefinitely();

            final String now = Instant.now().toString();
            execution.put("status", statusRunning);
            execution.put("resumedAt", now);
            execution.put("updatedAt", now);
            ProjectsExecutionLifecycleSupport.bumpExecutionVersion(execution);
            ProjectsFileStore.writeExecutions(executions);
            final Map<String, Object> resumeMeta = new HashMap<>();
            if (humanTaskId != null) {
                resumeMeta.put("humanTaskId", humanTaskId);
            }
            final String nodeId = ProjectsValueSupport.optionalStringValue(body.get("nodeId"));
            if (nodeId != null) {
                resumeMeta.put("nodeId", nodeId);
            } else {
                final String nestedNodeId = ProjectsValueSupport.optionalStringValue(resumeData.get("nodeId"));
                if (nestedNodeId != null) {
                    resumeMeta.put("nodeId", nestedNodeId);
                }
            }
            ProjectsFileStore.appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_RESUMED",
                    statusRunning,
                    "Execution resumed",
                    resumeMeta);

            return Response.ok(execution).build();
        } catch (Exception e) {
            return ProjectsExecutionLifecycleSupport.errorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    errorExecutionResumeFailed,
                    "Failed to resume execution",
                    true,
                    ProjectsExecutionLifecycleSupport.errorDetails(executionId, e),
                    ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
        }
    }

    static Response deleteExecution(
            String projectId,
            String executionId,
            String ifMatch,
            Long expectedVersionQuery,
            String errorExecutionNotFound,
            String errorExecutionVersionConflict,
            String errorExecutionDeleteFailed,
            long defaultRetryAfterSeconds) {
        try {
            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            final Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return ProjectsExecutionLifecycleSupport.errorResponse(
                        Response.Status.NOT_FOUND,
                        errorExecutionNotFound,
                        "Execution not found: " + executionId,
                        false,
                        Map.of("executionId", executionId),
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
            }
            final Long expectedVersion = ProjectsExecutionLifecycleSupport.resolveExpectedVersion(
                    ifMatch,
                    expectedVersionQuery);
            if (expectedVersion != null) {
                final Response versionConflict = ProjectsExecutionLifecycleSupport.validateExpectedVersion(
                        execution,
                        expectedVersion,
                        executionId,
                        errorExecutionVersionConflict,
                        ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
                if (versionConflict != null) {
                    return versionConflict;
                }
            }
            executions.remove(execution);
            ProjectsFileStore.writeExecutions(executions);
            ProjectsFileStore.appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_RECORD_DELETED",
                    "DELETED",
                    "Execution record deleted from standalone history",
                    Map.of());
            return Response.noContent().build();
        } catch (Exception e) {
            return ProjectsExecutionLifecycleSupport.errorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    errorExecutionDeleteFailed,
                    "Failed to delete execution",
                    true,
                    ProjectsExecutionLifecycleSupport.errorDetails(executionId, e),
                    ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds));
        }
    }

}
