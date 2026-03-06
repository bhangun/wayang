package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.domain.WayangDefinition;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.gamelan.GamelanWorkflowRunManager;
import tech.kayys.wayang.orchestrator.spi.WayangOrchestratorSpi;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.schema.validator.ValidationResult;
import tech.kayys.wayang.security.secrets.dto.RetrieveSecretRequest;
import tech.kayys.wayang.security.secrets.dto.Secret;
import tech.kayys.wayang.security.secrets.core.SecretManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Standalone-safe projects endpoint backed by local file storage.
 */
@Path("/api/v1/projects")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@IfBuildProperty(name = "wayang.runtime.standalone.projects-resource.enabled", stringValue = "true")
public class ProjectsResource {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP =
            new TypeReference<>() {};
    private static final String DEFAULT_TENANT = "community";
    private static final String DEFAULT_TYPE = "INTEGRATION";

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

    private static java.nio.file.Path storageFile() throws IOException {
        final String userHome = System.getProperty("user.home", ".");
        final java.nio.file.Path dir = Paths.get(userHome, ".wayang", "logs", "server");
        Files.createDirectories(dir);
        return dir.resolve("cloud-projects.json");
    }

    private static java.nio.file.Path executionsFile() throws IOException {
        final String userHome = System.getProperty("user.home", ".");
        final java.nio.file.Path dir = Paths.get(userHome, ".wayang", "logs", "server");
        Files.createDirectories(dir);
        return dir.resolve("cloud-project-executions.json");
    }

    private static java.nio.file.Path executionEventsFile() throws IOException {
        final String userHome = System.getProperty("user.home", ".");
        final java.nio.file.Path dir = Paths.get(userHome, ".wayang", "logs", "server");
        Files.createDirectories(dir);
        return dir.resolve("cloud-project-execution-events.json");
    }

    private static synchronized List<Map<String, Object>> readProjects() throws IOException {
        final java.nio.file.Path file = storageFile();
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        final String raw = Files.readString(file);
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        final List<Map<String, Object>> parsed = OBJECT_MAPPER.readValue(raw, LIST_OF_MAP);
        return parsed != null ? parsed : new ArrayList<>();
    }

    private static synchronized void writeProjects(List<Map<String, Object>> projects) throws IOException {
        final java.nio.file.Path file = storageFile();
        Files.writeString(file, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(projects));
    }

    private static synchronized List<Map<String, Object>> readExecutions() throws IOException {
        final java.nio.file.Path file = executionsFile();
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        final String raw = Files.readString(file);
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        final List<Map<String, Object>> parsed = OBJECT_MAPPER.readValue(raw, LIST_OF_MAP);
        return parsed != null ? parsed : new ArrayList<>();
    }

    private static synchronized void writeExecutions(List<Map<String, Object>> executions) throws IOException {
        final java.nio.file.Path file = executionsFile();
        Files.writeString(file, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(executions));
    }

    private static synchronized List<Map<String, Object>> readExecutionEvents() throws IOException {
        final java.nio.file.Path file = executionEventsFile();
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        final String raw = Files.readString(file);
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        final List<Map<String, Object>> parsed = OBJECT_MAPPER.readValue(raw, LIST_OF_MAP);
        return parsed != null ? parsed : new ArrayList<>();
    }

    private static synchronized void writeExecutionEvents(List<Map<String, Object>> events) throws IOException {
        final java.nio.file.Path file = executionEventsFile();
        Files.writeString(file, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(events));
    }

    private static synchronized void appendExecutionEvent(
            String projectId,
            String executionId,
            String type,
            String status,
            String message,
            Map<String, Object> metadata) throws IOException {
        final List<Map<String, Object>> events = readExecutionEvents();
        final Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("projectId", projectId);
        event.put("executionId", executionId);
        event.put("type", type);
        event.put("status", status);
        event.put("message", message);
        event.put("createdAt", Instant.now().toString());
        event.put("metadata", metadata != null ? metadata : Map.of());
        events.add(event);
        writeExecutionEvents(events);
    }

    @GET
    public List<Map<String, Object>> listProjects() {
        try {
            return readProjects();
        } catch (IOException ignored) {
            return List.of();
        }
    }

    @POST
    public Response createProject(Map<String, Object> request) {
        try {
            final Map<String, Object> body = request != null ? request : Map.of();
            final String now = Instant.now().toString();

            final Map<String, Object> project = new LinkedHashMap<>();
            project.put("projectId", UUID.randomUUID().toString());
            project.put("tenantId", stringValue(body.get("tenantId"), DEFAULT_TENANT));
            project.put("projectName", stringValue(body.get("projectName"), "Wayang Project"));
            project.put("description", stringValue(body.get("description"), ""));
            project.put("projectType", stringValue(body.get("projectType"), DEFAULT_TYPE));
            project.put("createdAt", now);
            project.put("updatedAt", now);
            project.put("savedAt", now);
            project.put("createdBy", stringValue(body.get("createdBy"), "wayang_designer"));
            project.put("source", "server");

            final Object metadata = body.get("metadata");
            if (metadata instanceof Map<?, ?> metaMap) {
                project.put("metadata", metaMap);
            } else {
                project.put("metadata", Map.of());
            }

            final List<Map<String, Object>> projects = readProjects();
            projects.removeIf(existing -> project.get("projectId").equals(existing.get("projectId")));
            projects.add(project);
            writeProjects(projects);

            return Response.status(Response.Status.CREATED).entity(project).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to persist project", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{projectId}")
    public Response getProject(@PathParam("projectId") String projectId) {
        try {
            final List<Map<String, Object>> projects = readProjects();
            return projects.stream()
                    .filter(p -> projectId.equals(String.valueOf(p.get("projectId"))))
                    .findFirst()
                    .<Response>map(p -> Response.ok(p).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read project", "message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{projectId}")
    public Response updateProject(
            @PathParam("projectId") String projectId,
            Map<String, Object> request) {
        try {
            final List<Map<String, Object>> projects = readProjects();
            Map<String, Object> target = null;
            for (Map<String, Object> project : projects) {
                if (projectId.equals(String.valueOf(project.get("projectId")))) {
                    target = project;
                    break;
                }
            }
            if (target == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            target.put("projectName", stringValue(body.get("projectName"), stringValue(target.get("projectName"), "Wayang Project")));
            target.put("description", stringValue(body.get("description"), stringValue(target.get("description"), "")));
            target.put("projectType", stringValue(body.get("projectType"), stringValue(target.get("projectType"), DEFAULT_TYPE)));
            target.put("tenantId", stringValue(body.get("tenantId"), stringValue(target.get("tenantId"), DEFAULT_TENANT)));
            target.put("updatedAt", Instant.now().toString());
            target.put("savedAt", Instant.now().toString());
            target.put("source", "server");

            final Object metadata = body.get("metadata");
            if (metadata instanceof Map<?, ?> metaMap) {
                target.put("metadata", metaMap);
            }

            writeProjects(projects);
            return Response.ok(target).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to update project", "message", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{projectId}")
    public Response deleteProject(@PathParam("projectId") String projectId) {
        try {
            final List<Map<String, Object>> projects = readProjects();
            final int before = projects.size();
            projects.removeIf(project -> projectId.equals(String.valueOf(project.get("projectId"))));
            if (projects.size() == before) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            writeProjects(projects);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to delete project", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{projectId}/executions")
    public Response listExecutions(@PathParam("projectId") String projectId) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            final List<Map<String, Object>> result = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId"))))
                    .sorted(Comparator.comparing((Map<String, Object> e) -> String.valueOf(e.getOrDefault("createdAt", "")))
                            .reversed())
                    .toList();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read executions", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{projectId}/executions/{executionId}")
    public Response getExecutionStatus(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }

            final String previousStatus = String.valueOf(execution.getOrDefault("status", "UNKNOWN"));
            final String latestStatus = definitionService.getExecutionStatus(executionId)
                    .await().indefinitely();
            execution.put("status", latestStatus != null ? latestStatus : "UNKNOWN");
            execution.put("updatedAt", Instant.now().toString());
            writeExecutions(executions);
            final String resolvedStatus = latestStatus != null ? latestStatus : "UNKNOWN";
            if (!resolvedStatus.equalsIgnoreCase(previousStatus)) {
                appendExecutionEvent(
                        projectId,
                        executionId,
                        "EXECUTION_STATUS_CHANGED",
                        resolvedStatus,
                        "Execution status changed from " + previousStatus + " to " + resolvedStatus,
                        Map.of(
                                "previousStatus", previousStatus,
                                "currentStatus", resolvedStatus));
            }
            return Response.ok(execution).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution status", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{projectId}/executions/{executionId}/events")
    public Response listExecutionEvents(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId) {
        try {
            final List<Map<String, Object>> events = readExecutionEvents().stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .sorted(Comparator.comparing(e -> String.valueOf(e.getOrDefault("createdAt", ""))))
                    .toList();
            return Response.ok(events).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution events", "message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{projectId}/execute-spec")
    public Response executeProjectSpec(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue(DEFAULT_TENANT) String tenantId,
            Map<String, Object> request) {
        return createExecution(projectId, tenantId, request);
    }

    @POST
    @Path("/{projectId}/executions")
    public Response createExecution(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue(DEFAULT_TENANT) String tenantId,
            Map<String, Object> request) {
        try {
            final List<Map<String, Object>> projects = readProjects();
            final boolean projectExists = projects.stream()
                    .anyMatch(project -> projectId.equals(String.valueOf(project.get("projectId"))));
            if (!projectExists) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Project not found: " + projectId))
                        .build();
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final Object rawSpec = body.containsKey("spec")
                    ? body.get("spec")
                    : body.containsKey("wayangSpec")
                            ? body.get("wayangSpec")
                            : body.get("workflowSpec");
            if (rawSpec == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Request body with 'spec' is required"))
                        .build();
            }

            final WayangSpec spec = OBJECT_MAPPER.convertValue(rawSpec, WayangSpec.class);
            final Map<String, Object> specPayloadRaw = OBJECT_MAPPER.convertValue(
                    spec, new TypeReference<Map<String, Object>>() {});
            final Map<String, Object> specPayload = pruneNulls(specPayloadRaw);
            final String schema = BuiltinSchemaCatalog.get(BuiltinSchemaCatalog.WAYANG_SPEC);
            final ValidationResult validation = schemaValidationService.validateSchema(schema, specPayload);
            if (!validation.isValid()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of(
                                "message", "WayangSpec validation failed",
                                "detail", validation.getMessage()))
                        .build();
            }

            final UUID projectUuid;
            try {
                projectUuid = UUID.fromString(projectId);
            } catch (IllegalArgumentException invalidProjectId) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Invalid projectId UUID: " + projectId))
                        .build();
            }

            final String definitionName = stringValue(body.get("name"), "project-" + projectId + "-spec");
            final String description = stringValue(body.get("description"), "");
            final String createdBy = stringValue(body.get("createdBy"), "api");
            final Map<String, Object> inputs = new HashMap<>(mapValue(body.get("inputs")));
            final SecretManager secretManager = resolveSecretManager();
            final Map<String, Object> agentConfigCoverage = summarizeAgentConfigCoverage(specPayload, tenantId, secretManager);
            if (!agentConfigCoverage.isEmpty()) {
                inputs.put("_agentConfigCoverage", agentConfigCoverage);
            }
            final Map<String, Object> resolvedCredentials = resolveCredentialInputs(specPayload, tenantId, secretManager);
            if (!resolvedCredentials.isEmpty()) {
                inputs.put("_resolvedCredentials", resolvedCredentials);
                inputs.put("_resolvedCredentialNames", resolvedCredentials.keySet().stream().toList());
            }

            String executionId;
            String workflowDefinitionId;
            String definitionIdValue;
            String executionSource = "standalone-projects-resource";

            try {
                final WayangDefinition definition = definitionService
                        .create(tenantId, projectUuid, definitionName, description, DefinitionType.WORKFLOW_TEMPLATE, spec,
                                createdBy)
                        .await().indefinitely();
                final WayangDefinition published = definitionService.publish(definition.definitionId, createdBy)
                        .await().indefinitely();
                executionId = definitionService.run(published.definitionId, inputs)
                        .await().indefinitely();
                workflowDefinitionId = published.workflowDefinitionId;
                definitionIdValue = published.definitionId.toString();
            } catch (RuntimeException persistenceFailure) {
                if (!isStandalonePersistenceUnavailable(persistenceFailure)) {
                    throw persistenceFailure;
                }
                if (orchestrator == null) {
                    throw persistenceFailure;
                }

                workflowDefinitionId = orchestrator.deploy(definitionName, spec).await().indefinitely();
                executionId = orchestrator.run(workflowDefinitionId, inputs).await().indefinitely();
                definitionIdValue = UUID.randomUUID().toString();
                executionSource = "standalone-orchestrator-fallback";
            }

            final String now = Instant.now().toString();
            final String finalExecutionId = executionId;
            final Map<String, Object> execution = new LinkedHashMap<>();
            execution.put("executionId", finalExecutionId);
            execution.put("projectId", projectId);
            execution.put("tenantId", tenantId);
            execution.put("definitionId", definitionIdValue);
            execution.put("workflowDefinitionId", workflowDefinitionId);
            execution.put("status", "STARTED");
            execution.put("name", definitionName);
            execution.put("description", description);
            execution.put("createdBy", createdBy);
            execution.put("createdAt", now);
            execution.put("updatedAt", now);
            execution.put("source", executionSource);
            if (!agentConfigCoverage.isEmpty()) {
                execution.put("agentConfigCoverage", agentConfigCoverage);
            }

            final List<Map<String, Object>> executions = readExecutions();
            executions.removeIf(existing -> finalExecutionId.equals(String.valueOf(existing.get("executionId"))));
            executions.add(execution);
            writeExecutions(executions);
            final Map<String, Object> startedMeta = new LinkedHashMap<>();
            startedMeta.put("definitionId", definitionIdValue);
            startedMeta.put("workflowDefinitionId", workflowDefinitionId);
            startedMeta.put("name", definitionName);
            if (!agentConfigCoverage.isEmpty()) {
                startedMeta.put("agentConfigCoverage", agentConfigCoverage);
            }
            if (!resolvedCredentials.isEmpty()) {
                startedMeta.put("resolvedCredentialCount", resolvedCredentials.size());
                startedMeta.put("resolvedCredentialNames", resolvedCredentials.keySet().stream().toList());
            }
            appendExecutionEvent(
                    projectId,
                    finalExecutionId,
                    "EXECUTION_STARTED",
                    "STARTED",
                    "Execution started",
                    startedMeta);

            return Response.accepted(Map.of(
                    "projectId", projectId,
                    "definitionId", definitionIdValue,
                    "workflowDefinitionId", workflowDefinitionId,
                    "executionId", finalExecutionId,
                    "status", "STARTED"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to execute project spec", "message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{projectId}/executions/{executionId}/stop")
    public Response stopExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }

            final boolean stopped = definitionService.stopExecution(executionId)
                    .await().indefinitely();
            if (!stopped) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("message", "Execution could not be stopped: " + executionId))
                        .build();
            }

            final String now = Instant.now().toString();
            execution.put("status", "STOPPED");
            execution.put("stoppedAt", now);
            execution.put("updatedAt", now);
            writeExecutions(executions);
            appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_STOPPED",
                    "STOPPED",
                    "Execution stop requested",
                    Map.of());

            return Response.ok(execution).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to stop execution", "message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{projectId}/executions/{executionId}/resume")
    public Response resumeExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            Map<String, Object> request) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final String humanTaskId = optionalStringValue(body.get("humanTaskId"));
            final Map<String, Object> resumeData = mapValue(body.get("data"));

            workflowRunManager.resumeRun(executionId, humanTaskId, resumeData)
                    .await().indefinitely();

            final String now = Instant.now().toString();
            execution.put("status", "RUNNING");
            execution.put("resumedAt", now);
            execution.put("updatedAt", now);
            writeExecutions(executions);
            final Map<String, Object> resumeMeta = new HashMap<>();
            if (humanTaskId != null) {
                resumeMeta.put("humanTaskId", humanTaskId);
            }
            final String nodeId = optionalStringValue(body.get("nodeId"));
            if (nodeId != null) {
                resumeMeta.put("nodeId", nodeId);
            } else {
                final String nestedNodeId = optionalStringValue(resumeData.get("nodeId"));
                if (nestedNodeId != null) {
                    resumeMeta.put("nodeId", nestedNodeId);
                }
            }
            appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_RESUMED",
                    "RUNNING",
                    "Execution resumed",
                    resumeMeta);

            return Response.ok(execution).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to resume execution", "message", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{projectId}/executions/{executionId}")
    public Response deleteExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            final int before = executions.size();
            executions.removeIf(e -> projectId.equals(String.valueOf(e.get("projectId")))
                    && executionId.equals(String.valueOf(e.get("executionId"))));
            if (executions.size() == before) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Execution not found: " + executionId))
                        .build();
            }
            writeExecutions(executions);
            appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_RECORD_DELETED",
                    "DELETED",
                    "Execution record deleted from standalone history",
                    Map.of());
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to delete execution", "message", e.getMessage()))
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            final Map<String, Object> result = new HashMap<>();
            rawMap.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return Map.of();
    }

    private static String stringValue(Object raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private static String optionalStringValue(Object raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private static boolean isStandalonePersistenceUnavailable(Throwable failure) {
        String message = failure != null ? String.valueOf(failure.getMessage()) : "";
        return message.contains("Mutiny.SessionFactory bean not found")
                || message.contains("Mutiny$SessionFactory")
                || message.contains("quarkus.hibernate-orm.enabled=false");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> pruneNulls(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return source.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> pruneValue(entry.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new));
    }

    @SuppressWarnings("unchecked")
    private static Object pruneValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> rawMap) {
            final Map<String, Object> cast = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> {
                if (k != null) {
                    cast.put(String.valueOf(k), v);
                }
            });
            return pruneNulls(cast);
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(ProjectsResource::pruneValue)
                    .filter(item -> item != null)
                    .toList();
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private SecretManager resolveSecretManager() {
        if (secretManagerInstance == null) {
            return null;
        }
        try {
            for (SecretManager manager : secretManagerInstance) {
                if (manager != null) {
                    return manager;
                }
            }
        } catch (Exception ignored) {
            // no-op; caller will treat as unavailable
        }
        return null;
    }

    private Map<String, Object> summarizeAgentConfigCoverage(
            Map<String, Object> specPayload,
            String tenantId,
            SecretManager secretManager) {
        if (specPayload == null || specPayload.isEmpty()) {
            return Map.of();
        }

        final Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("agentConfigNodes", 0);
        summary.put("providerModeAuto", 0);
        summary.put("providerModeLocal", 0);
        summary.put("providerModeCloud", 0);
        summary.put("localProviderConfigs", 0);
        summary.put("cloudProviderConfigs", 0);
        summary.put("credentialRefs", 0);
        summary.put("credentialRefsResolved", 0);
        summary.put("credentialRefsMissing", 0);
        summary.put("vaultConfigs", 0);

        final List<String> missingSecretPaths = new ArrayList<>();
        final Set<String> backends = new HashSet<>();
        final Map<String, String> rootVaultContext = extractRootVaultContext(specPayload, tenantId);
        scanConfigObject(specPayload, tenantId, rootVaultContext, summary, missingSecretPaths, backends, secretManager);

        int agentConfigNodes = intValue(summary.get("agentConfigNodes"));
        int credentialRefs = intValue(summary.get("credentialRefs"));
        int vaultConfigs = intValue(summary.get("vaultConfigs"));
        int localProviderConfigs = intValue(summary.get("localProviderConfigs"));
        int cloudProviderConfigs = intValue(summary.get("cloudProviderConfigs"));
        if (agentConfigNodes == 0
                && credentialRefs == 0
                && vaultConfigs == 0
                && localProviderConfigs == 0
                && cloudProviderConfigs == 0) {
            return Map.of();
        }

        summary.put("secretBackends", backends.stream().sorted().toList());
        if (!missingSecretPaths.isEmpty()) {
            summary.put("missingSecretPaths", missingSecretPaths);
        }
        summary.put("secretResolutionChecked", secretManager != null);
        return summary;
    }

    private Map<String, Object> resolveCredentialInputs(
            Map<String, Object> specPayload,
            String tenantId,
            SecretManager secretManager) {
        if (secretManager == null || specPayload == null || specPayload.isEmpty()) {
            return Map.of();
        }

        final List<Map<String, String>> refs = new ArrayList<>();
        final Map<String, String> rootVaultContext = extractRootVaultContext(specPayload, tenantId);
        collectCredentialRefs(specPayload, tenantId, rootVaultContext, refs);
        if (refs.isEmpty()) {
            return Map.of();
        }

        final Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map<String, String> ref : refs) {
            String name = ref.getOrDefault("name", "secret");
            String path = ref.get("path");
            if (path == null || path.isBlank()) {
                continue;
            }
            String refTenant = ref.getOrDefault("tenantId", tenantId);
            String key = ref.get("key");

            try {
                Secret secret = secretManager.retrieve(RetrieveSecretRequest.latest(refTenant, path)).await().indefinitely();
                if (secret == null || secret.data() == null || secret.data().isEmpty()) {
                    continue;
                }

                String value = null;
                if (key != null && !key.isBlank()) {
                    value = secret.data().get(key);
                }
                if ((value == null || value.isBlank()) && secret.data().size() == 1) {
                    value = secret.data().values().iterator().next();
                }
                if (value != null && !value.isBlank()) {
                    resolved.put(name, value);
                }
            } catch (Exception ignored) {
                // Keep execution flow non-fatal; unresolved secrets are reflected in coverage summary.
            }
        }

        return resolved;
    }

    @SuppressWarnings("unchecked")
    private void collectCredentialRefs(
            Object value,
            String defaultTenant,
            Map<String, String> inheritedVault,
            List<Map<String, String>> refs) {
        if (value == null) {
            return;
        }

        if (value instanceof List<?> list) {
            list.forEach(item -> collectCredentialRefs(item, defaultTenant, inheritedVault, refs));
            return;
        }

        if (!(value instanceof Map<?, ?> raw)) {
            return;
        }

        final Map<String, Object> map = new LinkedHashMap<>();
        raw.forEach((k, v) -> map.put(String.valueOf(k), v));

        Map<String, String> vaultContext = new HashMap<>(inheritedVault);
        applyVaultContext(vaultContext, mapValue(map.get("vault")));

        Object credentialRefs = map.get("credentialRefs");
        if (credentialRefs instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> refMap = mapValue(item);
                if (refMap.isEmpty()) {
                    continue;
                }
                final Map<String, String> ref = new LinkedHashMap<>();
                String name = optionalStringValue(refMap.get("name"));
                String path = optionalStringValue(refMap.get("path"));
                String key = optionalStringValue(refMap.get("key"));
                String tenant = optionalStringValue(refMap.get("tenantId"));

                if (name != null) {
                    ref.put("name", name);
                }
                if (key != null) {
                    ref.put("key", key);
                }
                if (tenant != null) {
                    ref.put("tenantId", tenant);
                } else if (vaultContext.containsKey("tenantId")) {
                    ref.put("tenantId", vaultContext.get("tenantId"));
                } else {
                    ref.put("tenantId", defaultTenant);
                }

                if (path != null) {
                    if (!path.startsWith("/") && vaultContext.containsKey("pathPrefix")) {
                        String prefix = vaultContext.get("pathPrefix");
                        if (prefix != null && !prefix.isBlank()) {
                            String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
                            if (path.equals(normalizedPrefix) || path.startsWith(normalizedPrefix + "/")) {
                                ref.put("path", path);
                            } else {
                                ref.put("path", normalizedPrefix + "/" + path);
                            }
                        } else {
                            ref.put("path", path);
                        }
                    } else {
                        ref.put("path", path);
                    }
                }

                refs.add(ref);
            }
        }

        map.values().forEach(v -> collectCredentialRefs(v, defaultTenant, vaultContext, refs));
    }

    @SuppressWarnings("unchecked")
    private void scanConfigObject(
            Object value,
            String tenantId,
            Map<String, String> inheritedVault,
            Map<String, Object> summary,
            List<String> missingSecretPaths,
            Set<String> backends,
            SecretManager secretManager) {
        if (value == null) {
            return;
        }
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> map.put(String.valueOf(k), v));
            Map<String, String> vaultContext = new HashMap<>(inheritedVault);

            boolean hasAgentConfigSignal = map.containsKey("providerMode")
                    || map.containsKey("localProvider")
                    || map.containsKey("cloudProvider")
                    || map.containsKey("credentialRefs")
                    || map.containsKey("vault");
            if (hasAgentConfigSignal) {
                increment(summary, "agentConfigNodes");
            }

            String providerMode = optionalStringValue(map.get("providerMode"));
            if (providerMode != null) {
                switch (providerMode.toLowerCase()) {
                    case "auto" -> increment(summary, "providerModeAuto");
                    case "local" -> increment(summary, "providerModeLocal");
                    case "cloud" -> increment(summary, "providerModeCloud");
                    default -> {
                        // keep backward/forward compatibility for unknown modes
                    }
                }
            }

            if (map.get("localProvider") instanceof Map<?, ?>) {
                increment(summary, "localProviderConfigs");
            }
            if (map.get("cloudProvider") instanceof Map<?, ?>) {
                increment(summary, "cloudProviderConfigs");
            }

            Map<String, Object> vaultConfig = mapValue(map.get("vault"));
            if (!vaultConfig.isEmpty()) {
                increment(summary, "vaultConfigs");
            }
            applyVaultContext(vaultContext, vaultConfig);
            String backend = optionalStringValue(vaultConfig.get("backend"));
            if (backend == null) {
                backend = vaultContext.get("backend");
            }
            if (backend != null) {
                backends.add(backend);
            }

            Object refs = map.get("credentialRefs");
            if (refs instanceof List<?> refList) {
                for (Object ref : refList) {
                    Map<String, Object> refMap = mapValue(ref);
                    if (refMap.isEmpty()) {
                        continue;
                    }
                    increment(summary, "credentialRefs");

                    backend = optionalStringValue(refMap.get("backend"));
                    if (backend == null) {
                        backend = vaultContext.get("backend");
                    }
                    if (backend != null) {
                        backends.add(backend);
                    }

                    String path = optionalStringValue(refMap.get("path"));
                    if (path != null && !path.startsWith("/") && vaultContext.containsKey("pathPrefix")) {
                        String prefix = vaultContext.get("pathPrefix");
                        if (prefix != null && !prefix.isBlank()) {
                            String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
                            if (!path.equals(normalizedPrefix) && !path.startsWith(normalizedPrefix + "/")) {
                                path = normalizedPrefix + "/" + path;
                            }
                        }
                    }
                    if (path == null) {
                        increment(summary, "credentialRefsMissing");
                        continue;
                    }

                    if (secretManager != null) {
                        try {
                            String refTenant = optionalStringValue(refMap.get("tenantId"));
                            if (refTenant == null) {
                                refTenant = vaultContext.get("tenantId");
                            }
                            if (refTenant == null) {
                                refTenant = tenantId;
                            }
                            boolean exists = secretManager.exists(refTenant, path).await().indefinitely();
                            if (exists) {
                                increment(summary, "credentialRefsResolved");
                            } else {
                                increment(summary, "credentialRefsMissing");
                                if (missingSecretPaths.size() < 10) {
                                    missingSecretPaths.add(path);
                                }
                            }
                        } catch (Exception ignored) {
                            increment(summary, "credentialRefsMissing");
                            if (missingSecretPaths.size() < 10) {
                                missingSecretPaths.add(path);
                            }
                        }
                    }
                }
            }

            map.values().forEach(v -> scanConfigObject(
                    v,
                    tenantId,
                    vaultContext,
                    summary,
                    missingSecretPaths,
                    backends,
                    secretManager));
            return;
        }
        if (value instanceof List<?> list) {
            list.forEach(v -> scanConfigObject(
                    v,
                    tenantId,
                    inheritedVault,
                    summary,
                    missingSecretPaths,
                    backends,
                    secretManager));
        }
    }

    private static Map<String, String> extractRootVaultContext(Map<String, Object> specPayload, String tenantId) {
        Map<String, String> context = new HashMap<>();
        if (tenantId != null && !tenantId.isBlank()) {
            context.put("tenantId", tenantId);
        }
        Map<String, Object> extensions = mapValue(specPayload.get("extensions"));
        applyVaultContext(context, mapValue(extensions.get("vault")));
        return context;
    }

    private static void applyVaultContext(Map<String, String> context, Map<String, Object> vaultConfig) {
        if (context == null || vaultConfig == null || vaultConfig.isEmpty()) {
            return;
        }
        String tenant = optionalStringValue(vaultConfig.get("tenantId"));
        String prefix = optionalStringValue(vaultConfig.get("pathPrefix"));
        String backend = optionalStringValue(vaultConfig.get("backend"));
        if (tenant != null) {
            context.put("tenantId", tenant);
        }
        if (prefix != null) {
            context.put("pathPrefix", prefix);
        }
        if (backend != null) {
            context.put("backend", backend);
        }
    }

    private static void increment(Map<String, Object> summary, String key) {
        summary.put(key, intValue(summary.get(key)) + 1);
    }

    private static int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
