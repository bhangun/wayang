package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.gamelan.GamelanWorkflowRunManager;
import tech.kayys.wayang.orchestrator.spi.WayangOrchestratorSpi;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.security.secrets.core.SecretManager;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class ProjectsExecutionFacade {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_TENANT = "community";
    private static final String DEFAULT_OWNER_USER = "wayang_designer";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_STOPPED = "STOPPED";
    private static final String ERROR_EXECUTION_NOT_FOUND = "EXECUTION_NOT_FOUND";
    private static final String ERROR_EXECUTION_INVALID_TRANSITION = "EXECUTION_INVALID_TRANSITION";
    private static final String ERROR_EXECUTION_STOP_FAILED = "EXECUTION_STOP_FAILED";
    private static final String ERROR_EXECUTION_RESUME_FAILED = "EXECUTION_RESUME_FAILED";
    private static final String ERROR_EXECUTION_DELETE_FAILED = "EXECUTION_DELETE_FAILED";
    private static final String ERROR_INVALID_STOP_REASON = "INVALID_STOP_REASON";
    private static final String ERROR_EXECUTION_VERSION_CONFLICT = "EXECUTION_VERSION_CONFLICT";
    private static final String ERROR_EXECUTION_RATE_LIMITED = "EXECUTION_RATE_LIMITED";
    private static final String ERROR_EXECUTION_BACKPRESSURE = "EXECUTION_BACKPRESSURE";
    private static final long DEFAULT_RETRY_AFTER_SECONDS = 2L;
    private static final long DEFAULT_IDEMPOTENCY_REPLAY_WINDOW_SECONDS = 86400L;
    private static final long DEFAULT_RATE_LIMIT_PER_MINUTE = 120L;
    private static final int DEFAULT_MAX_IN_FLIGHT_EXECUTION_SUBMITS = 64;
    private static final int DEFAULT_MAX_SUB_WORKFLOW_DEPTH = 2;
    private static final String VISIBILITY_PRIVATE = "private";
    private static final String VISIBILITY_TENANT = "tenant";
    private static final String VISIBILITY_EXPLICIT = "explicit";
    private static final String VISIBILITY_PUBLIC = "public";
    private static final String REQUIRED_PERMISSION_EXECUTE_SUBWORKFLOW = "execute_subworkflow";
    private static final String REUSE_MODE_CALLABLE = "callable";
    private static final String REUSE_MODE_AUTONOMOUS = "autonomous";
    private static final String ENTRYPOINT_MANUAL = "manual";
    private static final String ENTRYPOINT_PARAMETERIZED = "parameterized";
    private static final String ENTRYPOINT_EMPTY = "empty";
    private static final Set<String> CALLABLE_ENTRYPOINT_TYPES = Set.of(
            ENTRYPOINT_MANUAL,
            ENTRYPOINT_PARAMETERIZED,
            ENTRYPOINT_EMPTY);
    private static final Set<String> MANUAL_START_NODE_TYPES = Set.of(
            "start",
            "trigger-start",
            "trigger_start",
            "trigger-manual",
            "manual-start",
            "manual_start");
    private static final Set<String> PARAMETERIZED_START_NODE_TYPES = Set.of(
            "trigger-parameterized",
            "trigger_parameterized",
            "start-parameterized",
            "start_parameterized",
            "parameterized-start",
            "parameterized_start",
            "input-start",
            "input_start");
    private static final Set<String> AUTONOMOUS_TRIGGER_NODE_TYPES = Set.of(
            "trigger-schedule",
            "trigger-email",
            "trigger-telegram",
            "trigger-websocket",
            "trigger-webhook",
            "trigger-event",
            "trigger-kafka",
            "trigger-file");
    private static final Set<String> SUB_WORKFLOW_NODE_TYPES = Set.of(
            "sub-workflow",
            "sub_workflow",
            "custom-agent",
            "custom-agent-node",
            "agent-custom",
            "agent-custom-node");
    private static final ConcurrentHashMap<String, ProjectsExecutionLifecycleSupport.RateLimitWindow> RATE_LIMIT_WINDOWS =
            new ConcurrentHashMap<>();
    private static final AtomicInteger IN_FLIGHT_EXECUTION_SUBMITS = new AtomicInteger(0);
    private static final Set<String> SUPPORTED_STOP_REASONS = Set.of(
            "USER_REQUEST",
            "TIMEOUT",
            "POLICY_VIOLATION",
            "DEPENDENCY_FAILURE",
            "MANUAL_INTERVENTION",
            "UNKNOWN");

    private final WayangDefinitionService definitionService;
    private final SchemaValidationService schemaValidationService;
    private final GamelanWorkflowRunManager workflowRunManager;
    private final WayangOrchestratorSpi orchestrator;
    private final Instance<SecretManager> secretManagerInstance;

    ProjectsExecutionFacade(
            WayangDefinitionService definitionService,
            SchemaValidationService schemaValidationService,
            GamelanWorkflowRunManager workflowRunManager,
            WayangOrchestratorSpi orchestrator,
            Instance<SecretManager> secretManagerInstance) {
        this.definitionService = definitionService;
        this.schemaValidationService = schemaValidationService;
        this.workflowRunManager = workflowRunManager;
        this.orchestrator = orchestrator;
        this.secretManagerInstance = secretManagerInstance;
    }

    Response listExecutions(String projectId) {
        return ProjectsExecutionQuerySupport.listExecutions(projectId);
    }

    Response getExecutionStatus(String projectId, String executionId, String ifNoneMatch) {
        return ProjectsExecutionQuerySupport.refreshAndGetExecutionStatus(
                projectId,
                executionId,
                ifNoneMatch,
                definitionService,
                STATUS_UNKNOWN);
    }

    Response listExecutionEvents(String projectId, String executionId) {
        return ProjectsExecutionQuerySupport.listExecutionEvents(projectId, executionId);
    }

    Response getExecutionTelemetry(
            String projectId,
            String executionId,
            String from,
            String to,
            String nodeId,
            String type,
            String groupBy,
            String sort,
            Integer limit,
            boolean includeRaw) {
        return ProjectsExecutionQuerySupport.getExecutionTelemetry(
                projectId,
                executionId,
                from,
                to,
                nodeId,
                type,
                groupBy,
                sort,
                limit,
                includeRaw);
    }

    Response getExecutionLineage(
            String projectId,
            String executionId,
            String view,
            String nodeId,
            String sort,
            Integer limit,
            Integer offset,
            String fields,
            String include) {
        return ProjectsExecutionQuerySupport.getExecutionLineage(
                projectId,
                executionId,
                view,
                nodeId,
                sort,
                limit,
                offset,
                fields,
                include);
    }

    Response executeProjectSpec(
            String projectId,
            String tenantId,
            String userId,
            String requestId,
            Map<String, Object> request) {
        return createExecution(projectId, tenantId, userId, null, null, requestId, request);
    }

    Response createExecution(
            String projectId,
            String tenantId,
            String userId,
            String idempotencyKey,
            String xIdempotencyKey,
            String requestId,
            Map<String, Object> request) {
        return ProjectsExecutionSubmitSupport.createExecution(
                projectId,
                tenantId,
                userId,
                idempotencyKey,
                xIdempotencyKey,
                requestId,
                request,
                definitionService,
                schemaValidationService,
                workflowRunManager,
                orchestrator,
                secretManagerInstance,
                OBJECT_MAPPER,
                RATE_LIMIT_WINDOWS,
                IN_FLIGHT_EXECUTION_SUBMITS,
                DEFAULT_RATE_LIMIT_PER_MINUTE,
                DEFAULT_MAX_IN_FLIGHT_EXECUTION_SUBMITS,
                DEFAULT_RETRY_AFTER_SECONDS,
                DEFAULT_IDEMPOTENCY_REPLAY_WINDOW_SECONDS,
                DEFAULT_TENANT,
                this::resolveMaxSubWorkflowDepth,
                this::expandSubWorkflowReferences,
                ERROR_EXECUTION_RATE_LIMITED,
                ERROR_EXECUTION_BACKPRESSURE);
    }

    Response stopExecution(String projectId, String executionId, String ifMatch, Map<String, Object> request) {
        return ProjectsExecutionMutationSupport.stopExecution(
                projectId,
                executionId,
                ifMatch,
                request,
                definitionService,
                SUPPORTED_STOP_REASONS,
                STATUS_UNKNOWN,
                STATUS_STOPPED,
                ERROR_EXECUTION_NOT_FOUND,
                ERROR_EXECUTION_INVALID_TRANSITION,
                ERROR_EXECUTION_VERSION_CONFLICT,
                ERROR_EXECUTION_STOP_FAILED,
                ERROR_INVALID_STOP_REASON,
                DEFAULT_RETRY_AFTER_SECONDS);
    }

    Response resumeExecution(String projectId, String executionId, String ifMatch, Map<String, Object> request) {
        return ProjectsExecutionMutationSupport.resumeExecution(
                projectId,
                executionId,
                ifMatch,
                request,
                workflowRunManager,
                STATUS_UNKNOWN,
                STATUS_RUNNING,
                ERROR_EXECUTION_NOT_FOUND,
                ERROR_EXECUTION_INVALID_TRANSITION,
                ERROR_EXECUTION_VERSION_CONFLICT,
                ERROR_EXECUTION_RESUME_FAILED,
                DEFAULT_RETRY_AFTER_SECONDS);
    }

    Response deleteExecution(String projectId, String executionId, String ifMatch, Long expectedVersionQuery) {
        return ProjectsExecutionMutationSupport.deleteExecution(
                projectId,
                executionId,
                ifMatch,
                expectedVersionQuery,
                ERROR_EXECUTION_NOT_FOUND,
                ERROR_EXECUTION_VERSION_CONFLICT,
                ERROR_EXECUTION_DELETE_FAILED,
                DEFAULT_RETRY_AFTER_SECONDS);
    }

    private int resolveMaxSubWorkflowDepth(Map<String, Object> request, Map<String, Object> specPayload) {
        return ProjectsSubWorkflowResolverSupport.resolveMaxSubWorkflowDepth(
                request,
                specPayload,
                DEFAULT_MAX_SUB_WORKFLOW_DEPTH);
    }

    private Map<String, Object> expandSubWorkflowReferences(
            Map<String, Object> rawSpecPayload,
            String projectId,
            int maxDepth,
            String tenantId,
            String requesterUserId) throws IOException {
        return ProjectsSubWorkflowResolverSupport.expandSubWorkflowReferences(
                rawSpecPayload,
                projectId,
                maxDepth,
                tenantId,
                requesterUserId,
                DEFAULT_TENANT,
                DEFAULT_OWNER_USER,
                VISIBILITY_PRIVATE,
                VISIBILITY_TENANT,
                VISIBILITY_EXPLICIT,
                VISIBILITY_PUBLIC,
                REQUIRED_PERMISSION_EXECUTE_SUBWORKFLOW,
                REUSE_MODE_CALLABLE,
                REUSE_MODE_AUTONOMOUS,
                ENTRYPOINT_MANUAL,
                ENTRYPOINT_PARAMETERIZED,
                ENTRYPOINT_EMPTY,
                CALLABLE_ENTRYPOINT_TYPES,
                MANUAL_START_NODE_TYPES,
                PARAMETERIZED_START_NODE_TYPES,
                AUTONOMOUS_TRIGGER_NODE_TYPES,
                SUB_WORKFLOW_NODE_TYPES);
    }
}
