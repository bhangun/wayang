package tech.kayys.wayang.runtime.standalone.resource;

import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProjectsCrudCallableFacade {
    private static final String DEFAULT_TENANT = "community";
    private static final String DEFAULT_TYPE = "INTEGRATION";
    private static final String DEFAULT_OWNER_USER = "wayang_designer";
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

    List<Map<String, Object>> listProjects() {
        return ProjectsProjectCrudSupport.listProjects();
    }

    Response listShareableProjects(String tenantId, String userId, String mode, String excludeProjectId) {
        return ProjectsCallableContractSupport.listShareableProjects(
                tenantId,
                userId,
                mode,
                excludeProjectId,
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
                MANUAL_START_NODE_TYPES,
                PARAMETERIZED_START_NODE_TYPES,
                AUTONOMOUS_TRIGGER_NODE_TYPES);
    }

    Response createProject(Map<String, Object> request) {
        return ProjectsProjectCrudSupport.createProject(
                request,
                DEFAULT_TENANT,
                DEFAULT_OWNER_USER,
                DEFAULT_TYPE,
                VISIBILITY_PRIVATE,
                REUSE_MODE_CALLABLE,
                REUSE_MODE_AUTONOMOUS,
                ENTRYPOINT_MANUAL,
                ENTRYPOINT_PARAMETERIZED,
                ENTRYPOINT_EMPTY,
                MANUAL_START_NODE_TYPES,
                PARAMETERIZED_START_NODE_TYPES,
                AUTONOMOUS_TRIGGER_NODE_TYPES);
    }

    Response getProject(String projectId) {
        return ProjectsProjectCrudSupport.getProject(projectId);
    }

    Response getCallableContract(String projectId, String tenantId, String userId) {
        return ProjectsCallableContractSupport.getCallableContract(
                projectId,
                tenantId,
                userId,
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
                MANUAL_START_NODE_TYPES,
                PARAMETERIZED_START_NODE_TYPES,
                AUTONOMOUS_TRIGGER_NODE_TYPES);
    }

    Response validateCallableContract(String projectId, String tenantId, String userId, Map<String, Object> request) {
        return ProjectsCallableContractSupport.validateCallableContract(
                projectId,
                tenantId,
                userId,
                request,
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
                AUTONOMOUS_TRIGGER_NODE_TYPES);
    }

    Response previewOutputBindings(String projectId, String tenantId, String userId, Map<String, Object> request) {
        return ProjectsCallableContractSupport.previewOutputBindings(
                projectId,
                tenantId,
                userId,
                request,
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
                MANUAL_START_NODE_TYPES,
                PARAMETERIZED_START_NODE_TYPES,
                AUTONOMOUS_TRIGGER_NODE_TYPES);
    }

    Response updateProject(String projectId, Map<String, Object> request) {
        return ProjectsProjectCrudSupport.updateProject(
                projectId,
                request,
                DEFAULT_TENANT,
                DEFAULT_OWNER_USER,
                DEFAULT_TYPE,
                VISIBILITY_PRIVATE,
                REUSE_MODE_CALLABLE,
                REUSE_MODE_AUTONOMOUS,
                ENTRYPOINT_MANUAL,
                ENTRYPOINT_PARAMETERIZED,
                ENTRYPOINT_EMPTY,
                MANUAL_START_NODE_TYPES,
                PARAMETERIZED_START_NODE_TYPES,
                AUTONOMOUS_TRIGGER_NODE_TYPES);
    }

    Response deleteProject(String projectId) {
        return ProjectsProjectCrudSupport.deleteProject(projectId);
    }
}
