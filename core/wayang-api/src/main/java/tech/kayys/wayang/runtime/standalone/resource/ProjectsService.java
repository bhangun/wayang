package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.gamelan.GamelanWorkflowRunManager;
import tech.kayys.wayang.orchestrator.spi.WayangOrchestratorSpi;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.security.secrets.core.SecretManager;

import java.util.List;
import java.util.Map;

/**
 * Standalone-safe projects service backed by local file storage.
 */
@ApplicationScoped
public class ProjectsService {

    @Inject
    WayangDefinitionService definitionService;

    @Inject
    SchemaValidationService schemaValidationService;

    @Inject
    GamelanWorkflowRunManager workflowRunManager;

    @Inject
    WayangOrchestratorSpi orchestrator;

    @Inject
    Instance<SecretManager> secretManagerInstance;

    private final ProjectsCrudCallableFacade crudCallable = new ProjectsCrudCallableFacade();

    public List<Map<String, Object>> listProjects() {
        return crudCallable.listProjects();
    }

    public Response listShareableProjects(String tenantId, String userId, String mode, String excludeProjectId) {
        return crudCallable.listShareableProjects(tenantId, userId, mode, excludeProjectId);
    }

    public Response createProject(Map<String, Object> request) {
        return crudCallable.createProject(request);
    }

    public Response getProject(String projectId) {
        return crudCallable.getProject(projectId);
    }

    public Response getCallableContract(String projectId, String tenantId, String userId) {
        return crudCallable.getCallableContract(projectId, tenantId, userId);
    }

    public Response validateCallableContract(String projectId, String tenantId, String userId, Map<String, Object> request) {
        return crudCallable.validateCallableContract(projectId, tenantId, userId, request);
    }

    public Response previewOutputBindings(String projectId, String tenantId, String userId, Map<String, Object> request) {
        return crudCallable.previewOutputBindings(projectId, tenantId, userId, request);
    }

    public Response updateProject(String projectId, Map<String, Object> request) {
        return crudCallable.updateProject(projectId, request);
    }

    public Response deleteProject(String projectId) {
        return crudCallable.deleteProject(projectId);
    }

    public Response listExecutions(String projectId) {
        return execution().listExecutions(projectId);
    }

    public Response getExecutionStatus(String projectId, String executionId, String ifNoneMatch) {
        return execution().getExecutionStatus(projectId, executionId, ifNoneMatch);
    }

    Response getExecutionStatus(String projectId, String executionId) {
        return getExecutionStatus(projectId, executionId, null);
    }

    public Response listExecutionEvents(String projectId, String executionId) {
        return execution().listExecutionEvents(projectId, executionId);
    }

    public Response getExecutionTelemetry(
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
        return execution().getExecutionTelemetry(projectId, executionId, from, to, nodeId, type, groupBy, sort, limit, includeRaw);
    }

    public Response getExecutionLineage(
            String projectId,
            String executionId,
            String view,
            String nodeId,
            String sort,
            Integer limit,
            Integer offset,
            String fields,
            String include) {
        return execution().getExecutionLineage(projectId, executionId, view, nodeId, sort, limit, offset, fields, include);
    }

    Response getExecutionLineage(String projectId, String executionId) {
        return getExecutionLineage(projectId, executionId, "full", null, null, null, null, null, null);
    }

    public Response executeProjectSpec(
            String projectId,
            String tenantId,
            String userId,
            String requestId,
            Map<String, Object> request) {
        return execution().executeProjectSpec(projectId, tenantId, userId, requestId, request);
    }

    public Response createExecution(
            String projectId,
            String tenantId,
            String userId,
            String idempotencyKey,
            String xIdempotencyKey,
            String requestId,
            Map<String, Object> request) {
        return execution().createExecution(projectId, tenantId, userId, idempotencyKey, xIdempotencyKey, requestId, request);
    }

    Response createExecution(String projectId, String tenantId, Map<String, Object> request) {
        return createExecution(projectId, tenantId, null, null, null, null, request);
    }

    Response createExecution(
            String projectId,
            String tenantId,
            String idempotencyKey,
            String xIdempotencyKey,
            String requestId,
            Map<String, Object> request) {
        return createExecution(projectId, tenantId, null, idempotencyKey, xIdempotencyKey, requestId, request);
    }

    public Response stopExecution(String projectId, String executionId, String ifMatch, Map<String, Object> request) {
        return execution().stopExecution(projectId, executionId, ifMatch, request);
    }

    Response stopExecution(String projectId, String executionId) {
        return stopExecution(projectId, executionId, null, null);
    }

    Response stopExecution(String projectId, String executionId, Map<String, Object> request) {
        return stopExecution(projectId, executionId, null, request);
    }

    public Response resumeExecution(String projectId, String executionId, String ifMatch, Map<String, Object> request) {
        return execution().resumeExecution(projectId, executionId, ifMatch, request);
    }

    Response resumeExecution(String projectId, String executionId, Map<String, Object> request) {
        return resumeExecution(projectId, executionId, null, request);
    }

    public Response deleteExecution(String projectId, String executionId, String ifMatch, Long expectedVersionQuery) {
        return execution().deleteExecution(projectId, executionId, ifMatch, expectedVersionQuery);
    }

    Response deleteExecution(String projectId, String executionId) {
        return deleteExecution(projectId, executionId, null, null);
    }

    private ProjectsExecutionFacade execution() {
        return new ProjectsExecutionFacade(
                definitionService,
                schemaValidationService,
                workflowRunManager,
                orchestrator,
                secretManagerInstance);
    }
}
