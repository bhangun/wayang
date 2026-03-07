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
import jakarta.ws.rs.QueryParam;
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
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_STARTED = "STARTED";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_WAITING_FOR_HUMAN_INPUT = "WAITING_FOR_HUMAN_INPUT";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
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
    private static final Map<String, RateLimitWindow> RATE_LIMIT_WINDOWS = new ConcurrentHashMap<>();
    private static final AtomicInteger IN_FLIGHT_EXECUTION_SUBMITS = new AtomicInteger(0);
    private static final Set<String> SUPPORTED_STOP_REASONS = Set.of(
            "USER_REQUEST",
            "TIMEOUT",
            "POLICY_VIOLATION",
            "DEPENDENCY_FAILURE",
            "MANUAL_INTERVENTION",
            "UNKNOWN");

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
            @PathParam("executionId") String executionId,
            @HeaderParam("If-None-Match") String ifNoneMatch) {
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

            final String previousStatus = normalizeStatus(execution.getOrDefault("status", STATUS_UNKNOWN));
            final String latestStatus = normalizeStatus(definitionService.getExecutionStatus(executionId)
                    .await().indefinitely());
            String effectiveStatus = latestStatus;
            if (!isStatusTransitionAllowed(previousStatus, latestStatus)) {
                effectiveStatus = previousStatus;
                appendExecutionEvent(
                        projectId,
                        executionId,
                        "EXECUTION_STATUS_TRANSITION_REJECTED",
                        previousStatus,
                        "Rejected invalid execution status transition from " + previousStatus + " to " + latestStatus,
                        Map.of(
                                "previousStatus", previousStatus,
                                "attemptedStatus", latestStatus));
            }
            execution.put("status", effectiveStatus);
            execution.put("updatedAt", Instant.now().toString());
            execution.putIfAbsent("version", 1L);
            writeExecutions(executions);
            final String resolvedStatus = effectiveStatus;
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
                bumpExecutionVersion(execution);
                writeExecutions(executions);
            }

            final String etag = executionVersionEtag(execution);
            if (etagEquals(ifNoneMatch, etag)) {
                return Response.notModified().tag(etag).build();
            }
            return Response.ok(execution).tag(etag).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution status", "message", e.getMessage()))
                    .build();
        }
    }

    Response getExecutionStatus(
            String projectId,
            String executionId) {
        return getExecutionStatus(projectId, executionId, null);
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

    @GET
    @Path("/{projectId}/executions/{executionId}/telemetry")
    public Response getExecutionTelemetry(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("nodeId") String nodeId,
            @QueryParam("type") String type,
            @QueryParam("groupBy") String groupBy,
            @QueryParam("sort") String sort,
            @QueryParam("limit") Integer limit,
            @QueryParam("includeRaw") @DefaultValue("false") boolean includeRaw) {
        try {
            final Instant fromInstant = parseFilterInstant(from);
            final Instant toInstant = parseFilterInstant(to);
            final String nodeFilter = optionalStringValue(nodeId);
            final String typeFilter = optionalStringValue(type);
            final String groupByValue = optionalStringValue(groupBy);
            final String sortValue = optionalStringValue(sort);
            final int limitValue = limit != null ? Math.max(0, limit) : 0;
            final List<Map<String, Object>> events = readExecutionEvents().stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .filter(e -> eventMatchesFilter(e, fromInstant, toInstant, nodeFilter, typeFilter))
                    .sorted(Comparator.comparing(e -> String.valueOf(e.getOrDefault("createdAt", ""))))
                    .toList();
            return Response.ok(aggregateTelemetry(
                    projectId, executionId, events, from, to, nodeFilter, typeFilter,
                    groupByValue, sortValue, limitValue, includeRaw))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to read execution telemetry", "message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{projectId}/execute-spec")
    public Response executeProjectSpec(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue(DEFAULT_TENANT) String tenantId,
            @HeaderParam("X-Request-Id") String requestId,
            Map<String, Object> request) {
        return createExecution(projectId, tenantId, null, null, requestId, request);
    }

    @POST
    @Path("/{projectId}/executions")
    public Response createExecution(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue(DEFAULT_TENANT) String tenantId,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("X-Idempotency-Key") String xIdempotencyKey,
            @HeaderParam("X-Request-Id") String requestId,
            Map<String, Object> request) {
        final RateLimitDecision rateLimit = consumeRateLimit(tenantId);
        if (!rateLimit.allowed) {
            return rateLimitedResponse(rateLimit);
        }
        if (!acquireInFlightPermit()) {
            return backpressureResponse(rateLimit);
        }
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
            final boolean dryRun = booleanValue(body.get("dryRun"))
                    || booleanValue(body.get("validateOnly"));
            final String requestIdempotencyKey = optionalStringValue(body.get("idempotencyKey"));
            final String resolvedIdempotencyKey = firstNonBlank(
                    optionalStringValue(idempotencyKey),
                    optionalStringValue(xIdempotencyKey),
                    requestIdempotencyKey);
            final long idempotencyReplayWindowSeconds = longValue(
                    body.get("idempotencyReplayWindowSeconds"),
                    idempotencyReplayWindowSeconds());
            final String resolvedRequestId = firstNonBlank(
                    optionalStringValue(requestId),
                    optionalStringValue(body.get("requestId")),
                    UUID.randomUUID().toString());
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
            final Instant queuedAt = Instant.now();
            final SecretManager secretManager = resolveSecretManager();
            final Map<String, Object> agentConfigCoverage = summarizeAgentConfigCoverage(specPayload, tenantId, secretManager);
            final Map<String, Object> resolvedCredentials = resolveCredentialInputs(specPayload, tenantId, secretManager);

            if (dryRun) {
                final Map<String, Object> dryRunResponse = new LinkedHashMap<>();
                dryRunResponse.put("projectId", projectId);
                dryRunResponse.put("status", "DRY_RUN_VALID");
                dryRunResponse.put("requestId", resolvedRequestId);
                dryRunResponse.put("dryRun", true);
                dryRunResponse.put("validated", true);
                dryRunResponse.put("canExecute", true);
                dryRunResponse.put("schemaId", BuiltinSchemaCatalog.WAYANG_SPEC);
                if (resolvedIdempotencyKey != null) {
                    dryRunResponse.put("idempotencyKey", resolvedIdempotencyKey);
                }
                dryRunResponse.put("name", definitionName);
                dryRunResponse.put("description", description);
                dryRunResponse.put("createdBy", createdBy);
                dryRunResponse.put("inputCount", inputs.size());
                dryRunResponse.put("timestamp", Instant.now().toString());
                if (!agentConfigCoverage.isEmpty()) {
                    dryRunResponse.put("agentConfigCoverage", agentConfigCoverage);
                }
                if (!resolvedCredentials.isEmpty()) {
                    dryRunResponse.put("resolvedCredentialCount", resolvedCredentials.size());
                    dryRunResponse.put("resolvedCredentialNames", resolvedCredentials.keySet().stream().toList());
                }
                return addRateLimitHeaders(Response.ok(dryRunResponse), rateLimit).build();
            }

            if (resolvedIdempotencyKey != null && isIdempotencyEnabled()) {
                final List<Map<String, Object>> executions = readExecutions();
                final Instant nowInstant = Instant.now();
                final Map<String, Object> existingExecution = executions.stream()
                        .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                                && tenantId.equals(String.valueOf(e.getOrDefault("tenantId", DEFAULT_TENANT)))
                                && resolvedIdempotencyKey.equals(String.valueOf(e.get("idempotencyKey"))))
                        .filter(e -> isWithinIdempotencyReplayWindow(e, nowInstant, idempotencyReplayWindowSeconds))
                        .max(Comparator.comparing(e -> parseInstantOrEpoch(e.get("createdAt"))))
                        .orElse(null);
                if (existingExecution != null) {
                    final Map<String, Object> replay = new LinkedHashMap<>(existingExecution);
                    replay.put("idempotentReplay", true);
                    replay.putIfAbsent("requestId", resolvedRequestId);
                    replay.put("idempotencyReplayWindowSeconds", idempotencyReplayWindowSeconds);
                    replay.put("idempotencyReplayAgeSeconds",
                            Duration.between(parseInstantOrEpoch(existingExecution.get("createdAt")), nowInstant).getSeconds());
                    return addRateLimitHeaders(Response.ok(replay), rateLimit).build();
                }
            }

            if (!agentConfigCoverage.isEmpty()) {
                inputs.put("_agentConfigCoverage", agentConfigCoverage);
            }
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

            final Instant startedAt = Instant.now();
            final long queueDurationMs = Math.max(0L, Duration.between(queuedAt, startedAt).toMillis());
            final String now = Instant.now().toString();
            final String finalExecutionId = executionId;
            final Map<String, Object> execution = new LinkedHashMap<>();
            execution.put("executionId", finalExecutionId);
            execution.put("projectId", projectId);
            execution.put("tenantId", tenantId);
            execution.put("requestId", resolvedRequestId);
            execution.put("definitionId", definitionIdValue);
            execution.put("workflowDefinitionId", workflowDefinitionId);
            execution.put("status", "STARTED");
            execution.put("version", 1L);
            execution.put("name", definitionName);
            execution.put("description", description);
            execution.put("createdBy", createdBy);
            execution.put("createdAt", now);
            execution.put("queuedAt", queuedAt.toString());
            execution.put("startedAt", startedAt.toString());
            execution.put("queueDurationMs", queueDurationMs);
            execution.put("updatedAt", now);
            execution.put("source", executionSource);
            if (resolvedIdempotencyKey != null) {
                execution.put("idempotencyKey", resolvedIdempotencyKey);
                execution.put("idempotencyReplayWindowSeconds", idempotencyReplayWindowSeconds);
                if (idempotencyReplayWindowSeconds > 0) {
                    execution.put("idempotencyExpiresAt", startedAt.plusSeconds(idempotencyReplayWindowSeconds).toString());
                }
            }
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
            startedMeta.put("requestId", resolvedRequestId);
            startedMeta.put("queuedAt", queuedAt.toString());
            startedMeta.put("startedAt", startedAt.toString());
            startedMeta.put("queueDurationMs", queueDurationMs);
            if (resolvedIdempotencyKey != null) {
                startedMeta.put("idempotencyKey", resolvedIdempotencyKey);
                startedMeta.put("idempotencyReplayWindowSeconds", idempotencyReplayWindowSeconds);
                if (idempotencyReplayWindowSeconds > 0) {
                    startedMeta.put("idempotencyExpiresAt", startedAt.plusSeconds(idempotencyReplayWindowSeconds).toString());
                }
            }
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
                    "EXECUTION_QUEUED",
                    STATUS_QUEUED,
                    "Execution queued",
                    startedMeta);
            appendExecutionEvent(
                    projectId,
                    finalExecutionId,
                    "EXECUTION_STARTED",
                    STATUS_STARTED,
                    "Execution started",
                    startedMeta);

            final Map<String, Object> accepted = new LinkedHashMap<>();
            accepted.put("projectId", projectId);
            accepted.put("definitionId", definitionIdValue);
            accepted.put("workflowDefinitionId", workflowDefinitionId);
            accepted.put("executionId", finalExecutionId);
            accepted.put("requestId", resolvedRequestId);
            accepted.put("status", "STARTED");
            accepted.put("queuedAt", queuedAt.toString());
            accepted.put("startedAt", startedAt.toString());
            accepted.put("queueDurationMs", queueDurationMs);
            if (resolvedIdempotencyKey != null) {
                accepted.put("idempotencyKey", resolvedIdempotencyKey);
                accepted.put("idempotencyReplayWindowSeconds", idempotencyReplayWindowSeconds);
                if (idempotencyReplayWindowSeconds > 0) {
                    accepted.put("idempotencyExpiresAt", startedAt.plusSeconds(idempotencyReplayWindowSeconds).toString());
                }
            }
            return addRateLimitHeaders(Response.accepted(accepted), rateLimit).build();
        } catch (IllegalArgumentException e) {
            return addRateLimitHeaders(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    , rateLimit).build();
        } catch (IllegalStateException e) {
            return addRateLimitHeaders(Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", e.getMessage()))
                    , rateLimit).build();
        } catch (Exception e) {
            return addRateLimitHeaders(Response.serverError()
                    .entity(Map.of("error", "Failed to execute project spec", "message", e.getMessage()))
                    , rateLimit).build();
        } finally {
            releaseInFlightPermit();
        }
    }

    Response createExecution(
            String projectId,
            String tenantId,
            Map<String, Object> request) {
        return createExecution(projectId, tenantId, null, null, null, request);
    }

    @POST
    @Path("/{projectId}/executions/{executionId}/stop")
    public Response stopExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-Match") String ifMatch,
            Map<String, Object> request) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return errorResponse(
                        Response.Status.NOT_FOUND,
                        ERROR_EXECUTION_NOT_FOUND,
                        "Execution not found: " + executionId,
                        false,
                        Map.of("executionId", executionId));
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final Long expectedVersion = resolveExpectedVersion(ifMatch, body.get("expectedVersion"));
            if (expectedVersion != null) {
                final Response versionConflict = validateExpectedVersion(execution, expectedVersion, executionId);
                if (versionConflict != null) {
                    return versionConflict;
                }
            }
            final String stopReason = resolveStopReason(body.get("reason"));
            if (stopReason == null) {
                return errorResponse(
                        Response.Status.BAD_REQUEST,
                        ERROR_INVALID_STOP_REASON,
                        "Unsupported stop reason",
                        false,
                        Map.of(
                                "supportedReasons", SUPPORTED_STOP_REASONS,
                                "providedReason", optionalStringValue(body.get("reason"))));
            }
            final String stopNote = optionalStringValue(body.get("note"));
            final String currentStatus = normalizeStatus(execution.getOrDefault("status", STATUS_UNKNOWN));
            if (STATUS_STOPPED.equals(currentStatus)) {
                return transitionError(executionId, currentStatus, STATUS_STOPPED);
            }
            if (!isStatusTransitionAllowed(currentStatus, STATUS_STOPPED)) {
                return transitionError(executionId, currentStatus, STATUS_STOPPED);
            }

            final boolean stopped = definitionService.stopExecution(executionId)
                    .await().indefinitely();
            if (!stopped) {
                return errorResponse(
                        Response.Status.CONFLICT,
                        ERROR_EXECUTION_STOP_FAILED,
                        "Execution could not be stopped: " + executionId,
                        true,
                        Map.of("executionId", executionId, "reason", stopReason));
            }

            final String now = Instant.now().toString();
            execution.put("status", STATUS_STOPPED);
            execution.put("stoppedAt", now);
            execution.put("stopReason", stopReason);
            if (stopNote != null) {
                execution.put("stopNote", stopNote);
            }
            execution.put("updatedAt", now);
            bumpExecutionVersion(execution);
            writeExecutions(executions);
            final Map<String, Object> stopMeta = new LinkedHashMap<>();
            stopMeta.put("reason", stopReason);
            if (stopNote != null) {
                stopMeta.put("note", stopNote);
            }
            appendExecutionEvent(
                    projectId,
                    executionId,
                    "EXECUTION_STOPPED",
                    STATUS_STOPPED,
                    "Execution stop requested",
                    stopMeta);

            return Response.ok(execution).build();
        } catch (Exception e) {
            return errorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    ERROR_EXECUTION_STOP_FAILED,
                    "Failed to stop execution",
                    true,
                    errorDetails(executionId, e));
        }
    }

    Response stopExecution(
            String projectId,
            String executionId) {
        return stopExecution(projectId, executionId, null, null);
    }

    Response stopExecution(
            String projectId,
            String executionId,
            Map<String, Object> request) {
        return stopExecution(projectId, executionId, null, request);
    }

    @POST
    @Path("/{projectId}/executions/{executionId}/resume")
    public Response resumeExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-Match") String ifMatch,
            Map<String, Object> request) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return errorResponse(
                        Response.Status.NOT_FOUND,
                        ERROR_EXECUTION_NOT_FOUND,
                        "Execution not found: " + executionId,
                        false,
                        Map.of("executionId", executionId));
            }

            final String currentStatus = normalizeStatus(execution.getOrDefault("status", STATUS_UNKNOWN));
            if (STATUS_RUNNING.equals(currentStatus)) {
                return transitionError(executionId, currentStatus, STATUS_RUNNING);
            }
            if (!isStatusTransitionAllowed(currentStatus, STATUS_RUNNING)) {
                return transitionError(executionId, currentStatus, STATUS_RUNNING);
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final Long expectedVersion = resolveExpectedVersion(ifMatch, body.get("expectedVersion"));
            if (expectedVersion != null) {
                final Response versionConflict = validateExpectedVersion(execution, expectedVersion, executionId);
                if (versionConflict != null) {
                    return versionConflict;
                }
            }
            final String humanTaskId = optionalStringValue(body.get("humanTaskId"));
            final Map<String, Object> resumeData = mapValue(body.get("data"));

            workflowRunManager.resumeRun(executionId, humanTaskId, resumeData)
                    .await().indefinitely();

            final String now = Instant.now().toString();
            execution.put("status", STATUS_RUNNING);
            execution.put("resumedAt", now);
            execution.put("updatedAt", now);
            bumpExecutionVersion(execution);
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
                    STATUS_RUNNING,
                    "Execution resumed",
                    resumeMeta);

            return Response.ok(execution).build();
        } catch (Exception e) {
            return errorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    ERROR_EXECUTION_RESUME_FAILED,
                    "Failed to resume execution",
                    true,
                    errorDetails(executionId, e));
        }
    }

    Response resumeExecution(
            String projectId,
            String executionId,
            Map<String, Object> request) {
        return resumeExecution(projectId, executionId, null, request);
    }

    @DELETE
    @Path("/{projectId}/executions/{executionId}")
    public Response deleteExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-Match") String ifMatch,
            @QueryParam("expectedVersion") Long expectedVersionQuery) {
        try {
            final List<Map<String, Object>> executions = readExecutions();
            final Map<String, Object> execution = executions.stream()
                    .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                            && executionId.equals(String.valueOf(e.get("executionId"))))
                    .findFirst()
                    .orElse(null);
            if (execution == null) {
                return errorResponse(
                        Response.Status.NOT_FOUND,
                        ERROR_EXECUTION_NOT_FOUND,
                        "Execution not found: " + executionId,
                        false,
                        Map.of("executionId", executionId));
            }
            final Long expectedVersion = resolveExpectedVersion(ifMatch, expectedVersionQuery);
            if (expectedVersion != null) {
                final Response versionConflict = validateExpectedVersion(execution, expectedVersion, executionId);
                if (versionConflict != null) {
                    return versionConflict;
                }
            }
            executions.remove(execution);
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
            return errorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    ERROR_EXECUTION_DELETE_FAILED,
                    "Failed to delete execution",
                    true,
                    errorDetails(executionId, e));
        }
    }

    Response deleteExecution(
            String projectId,
            String executionId) {
        return deleteExecution(projectId, executionId, null, null);
    }

    private static Response transitionError(String executionId, String fromStatus, String toStatus) {
        return errorResponse(
                Response.Status.CONFLICT,
                ERROR_EXECUTION_INVALID_TRANSITION,
                "Invalid execution status transition",
                false,
                Map.of(
                        "executionId", executionId,
                        "fromStatus", fromStatus,
                        "toStatus", toStatus));
    }

    private static Response errorResponse(
            Response.Status status,
            String code,
            String message,
            boolean retryable,
            Map<String, Object> details) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", code);
        payload.put("message", message);
        payload.put("httpStatus", status.getStatusCode());
        payload.put("retryable", retryable);
        payload.put("timestamp", Instant.now().toString());
        payload.put("details", details != null ? details : Map.of());
        final Response.ResponseBuilder builder = Response.status(status).entity(payload);
        if (retryable) {
            final long retryAfterSeconds = retryAfterSeconds();
            payload.put("retryAfterSeconds", retryAfterSeconds);
            builder.header("Retry-After", String.valueOf(retryAfterSeconds));
        }
        return builder.build();
    }

    private static Response.ResponseBuilder addRateLimitHeaders(Response.ResponseBuilder builder, RateLimitDecision rateLimit) {
        if (builder == null || rateLimit == null) {
            return builder;
        }
        builder.header("X-RateLimit-Limit", String.valueOf(rateLimit.limit));
        builder.header("X-RateLimit-Remaining", String.valueOf(Math.max(0L, rateLimit.remaining)));
        builder.header("X-RateLimit-Reset", String.valueOf(rateLimit.resetEpochSeconds));
        return builder;
    }

    private static Response rateLimitedResponse(RateLimitDecision rateLimit) {
        final long retryAfter = Math.max(1L, rateLimit.retryAfterSeconds);
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("limit", rateLimit.limit);
        details.put("remaining", 0L);
        details.put("resetEpochSeconds", rateLimit.resetEpochSeconds);
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", ERROR_EXECUTION_RATE_LIMITED);
        payload.put("message", "Execution submit rate limit exceeded");
        payload.put("httpStatus", Response.Status.TOO_MANY_REQUESTS.getStatusCode());
        payload.put("retryable", true);
        payload.put("retryAfterSeconds", retryAfter);
        payload.put("timestamp", Instant.now().toString());
        payload.put("details", details);
        return addRateLimitHeaders(
                Response.status(Response.Status.TOO_MANY_REQUESTS)
                        .header("Retry-After", String.valueOf(retryAfter))
                        .entity(payload),
                rateLimit).build();
    }

    private static Response backpressureResponse(RateLimitDecision rateLimit) {
        final long retryAfter = retryAfterSeconds();
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("maxInFlight", maxInFlightExecutionSubmits());
        details.put("currentInFlight", IN_FLIGHT_EXECUTION_SUBMITS.get());
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", ERROR_EXECUTION_BACKPRESSURE);
        payload.put("message", "Execution submit backpressure: too many in-flight submits");
        payload.put("httpStatus", Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        payload.put("retryable", true);
        payload.put("retryAfterSeconds", retryAfter);
        payload.put("timestamp", Instant.now().toString());
        payload.put("details", details);
        return addRateLimitHeaders(
                Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .header("Retry-After", String.valueOf(retryAfter))
                        .entity(payload),
                rateLimit).build();
    }

    private static Map<String, Object> errorDetails(String executionId, Exception e) {
        final Map<String, Object> details = new LinkedHashMap<>();
        details.put("executionId", executionId);
        final String detail = optionalStringValue(e != null ? e.getMessage() : null);
        if (detail != null) {
            details.put("detail", detail);
        }
        return details;
    }

    private static String resolveStopReason(Object rawReason) {
        final String provided = optionalStringValue(rawReason);
        if (provided == null) {
            return "USER_REQUEST";
        }
        final String normalized = provided.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return SUPPORTED_STOP_REASONS.contains(normalized) ? normalized : null;
    }

    private static Long resolveExpectedVersion(String ifMatch, Object expectedVersionRaw) {
        final Long headerVersion = parseIfMatchVersion(ifMatch);
        if (headerVersion != null) {
            return headerVersion;
        }
        if (expectedVersionRaw == null) {
            return null;
        }
        if (expectedVersionRaw instanceof Number numeric) {
            return numeric.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(expectedVersionRaw).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Long parseIfMatchVersion(String ifMatch) {
        final String raw = optionalStringValue(ifMatch);
        if (raw == null || "*".equals(raw)) {
            return null;
        }
        String value = raw.trim();
        if (value.startsWith("W/")) {
            value = value.substring(2).trim();
        }
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean etagEquals(String ifNoneMatch, String currentEtag) {
        final String raw = optionalStringValue(ifNoneMatch);
        if (raw == null || currentEtag == null) {
            return false;
        }
        if ("*".equals(raw.trim())) {
            return true;
        }
        final String[] candidates = raw.split(",");
        for (String candidate : candidates) {
            if (candidate != null && normalizeEtagValue(candidate).equals(normalizeEtagValue(currentEtag))) {
                return true;
            }
        }
        return false;
    }

    private static String executionVersionEtag(Map<String, Object> execution) {
        return String.valueOf(executionVersion(execution));
    }

    private static String normalizeEtagValue(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim();
        if (value.startsWith("W/")) {
            value = value.substring(2).trim();
        }
        while (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static long executionVersion(Map<String, Object> execution) {
        return longValue(execution != null ? execution.get("version") : null, 1L);
    }

    private static void bumpExecutionVersion(Map<String, Object> execution) {
        if (execution == null) {
            return;
        }
        execution.put("version", executionVersion(execution) + 1L);
    }

    private static Response validateExpectedVersion(
            Map<String, Object> execution,
            long expectedVersion,
            String executionId) {
        final long currentVersion = executionVersion(execution);
        if (expectedVersion == currentVersion) {
            return null;
        }
        return errorResponse(
                Response.Status.CONFLICT,
                ERROR_EXECUTION_VERSION_CONFLICT,
                "Execution version conflict",
                true,
                Map.of(
                        "executionId", executionId,
                        "expectedVersion", expectedVersion,
                        "currentVersion", currentVersion));
    }

    private static boolean isIdempotencyEnabled() {
        final String raw = System.getProperty("wayang.runtime.standalone.execution.idempotency.enabled", "true");
        return booleanValue(raw);
    }

    private static long idempotencyReplayWindowSeconds() {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.idempotency.replay-window-seconds",
                String.valueOf(DEFAULT_IDEMPOTENCY_REPLAY_WINDOW_SECONDS));
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return DEFAULT_IDEMPOTENCY_REPLAY_WINDOW_SECONDS;
        }
    }

    private static boolean isWithinIdempotencyReplayWindow(
            Map<String, Object> execution,
            Instant now,
            long replayWindowSeconds) {
        if (replayWindowSeconds <= 0) {
            return false;
        }
        final Instant createdAt = parseInstantOrEpoch(execution.get("createdAt"));
        final long age = Math.max(0L, Duration.between(createdAt, now).getSeconds());
        return age <= replayWindowSeconds;
    }

    private static Instant parseInstantOrEpoch(Object raw) {
        final String value = optionalStringValue(raw);
        if (value == null) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return Instant.EPOCH;
        }
    }

    private static long longValue(Object raw, long fallback) {
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(raw.toString().trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static long retryAfterSeconds() {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.retry-after-seconds",
                String.valueOf(DEFAULT_RETRY_AFTER_SECONDS));
        try {
            final long parsed = Long.parseLong(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_RETRY_AFTER_SECONDS;
        } catch (Exception ignored) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }

    private static boolean rateLimitEnabled() {
        final String raw = System.getProperty("wayang.runtime.standalone.execution.rate-limit.enabled", "true");
        return booleanValue(raw);
    }

    private static long rateLimitPerMinute() {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.rate-limit.per-minute",
                String.valueOf(DEFAULT_RATE_LIMIT_PER_MINUTE));
        try {
            final long parsed = Long.parseLong(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_RATE_LIMIT_PER_MINUTE;
        } catch (Exception ignored) {
            return DEFAULT_RATE_LIMIT_PER_MINUTE;
        }
    }

    private static int maxInFlightExecutionSubmits() {
        final String raw = System.getProperty(
                "wayang.runtime.standalone.execution.max-in-flight-submits",
                String.valueOf(DEFAULT_MAX_IN_FLIGHT_EXECUTION_SUBMITS));
        try {
            final int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : DEFAULT_MAX_IN_FLIGHT_EXECUTION_SUBMITS;
        } catch (Exception ignored) {
            return DEFAULT_MAX_IN_FLIGHT_EXECUTION_SUBMITS;
        }
    }

    private static RateLimitDecision consumeRateLimit(String tenantId) {
        final long nowMs = System.currentTimeMillis();
        final long nowEpoch = Instant.ofEpochMilli(nowMs).getEpochSecond();
        final long limit = rateLimitPerMinute();
        final long windowMillis = 60_000L;
        final String key = optionalStringValue(tenantId) != null ? tenantId : DEFAULT_TENANT;
        if (!rateLimitEnabled()) {
            return new RateLimitDecision(true, limit, limit, nowEpoch + 60L, 0L);
        }
        final RateLimitWindow window = RATE_LIMIT_WINDOWS.computeIfAbsent(key, ignored -> new RateLimitWindow());
        synchronized (window) {
            if (window.windowStartMillis == 0L || nowMs - window.windowStartMillis >= windowMillis) {
                window.windowStartMillis = nowMs;
                window.count = 0L;
            }
            final long resetEpoch = Instant.ofEpochMilli(window.windowStartMillis + windowMillis).getEpochSecond();
            if (window.count >= limit) {
                final long retryAfter = Math.max(1L, (window.windowStartMillis + windowMillis - nowMs + 999L) / 1000L);
                return new RateLimitDecision(false, limit, 0L, resetEpoch, retryAfter);
            }
            window.count++;
            final long remaining = Math.max(0L, limit - window.count);
            return new RateLimitDecision(true, limit, remaining, resetEpoch, 0L);
        }
    }

    private static boolean acquireInFlightPermit() {
        final int max = maxInFlightExecutionSubmits();
        while (true) {
            int current = IN_FLIGHT_EXECUTION_SUBMITS.get();
            if (current >= max) {
                return false;
            }
            if (IN_FLIGHT_EXECUTION_SUBMITS.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    private static void releaseInFlightPermit() {
        while (true) {
            int current = IN_FLIGHT_EXECUTION_SUBMITS.get();
            if (current <= 0) {
                return;
            }
            if (IN_FLIGHT_EXECUTION_SUBMITS.compareAndSet(current, current - 1)) {
                return;
            }
        }
    }

    private static final class RateLimitWindow {
        private long windowStartMillis;
        private long count;
    }

    private record RateLimitDecision(
            boolean allowed,
            long limit,
            long remaining,
            long resetEpochSeconds,
            long retryAfterSeconds) {
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

    private static String normalizeStatus(Object rawStatus) {
        if (rawStatus == null) {
            return STATUS_UNKNOWN;
        }
        final String status = rawStatus.toString().trim();
        if (status.isEmpty()) {
            return STATUS_UNKNOWN;
        }
        return status.toUpperCase().replace('-', '_').replace(' ', '_');
    }

    private static boolean isTerminalStatus(String status) {
        return STATUS_COMPLETED.equals(status) || STATUS_FAILED.equals(status) || STATUS_STOPPED.equals(status);
    }

    private static boolean isStatusTransitionAllowed(String fromRaw, String toRaw) {
        final String from = normalizeStatus(fromRaw);
        final String to = normalizeStatus(toRaw);

        if (STATUS_UNKNOWN.equals(from) || STATUS_UNKNOWN.equals(to)) {
            return true;
        }
        if (from.equals(to)) {
            return true;
        }
        if (isTerminalStatus(from)) {
            return false;
        }

        return switch (from) {
            case STATUS_QUEUED -> Set.of(STATUS_STARTED, STATUS_RUNNING, STATUS_FAILED, STATUS_STOPPED).contains(to);
            case STATUS_STARTED -> Set.of(STATUS_RUNNING, STATUS_COMPLETED, STATUS_FAILED, STATUS_STOPPED, STATUS_PAUSED,
                    STATUS_WAITING_FOR_HUMAN_INPUT).contains(to);
            case STATUS_RUNNING -> Set.of(STATUS_COMPLETED, STATUS_FAILED, STATUS_STOPPED, STATUS_PAUSED,
                    STATUS_WAITING_FOR_HUMAN_INPUT).contains(to);
            case STATUS_PAUSED, STATUS_WAITING_FOR_HUMAN_INPUT ->
                Set.of(STATUS_RUNNING, STATUS_FAILED, STATUS_STOPPED).contains(to);
            default -> true;
        };
    }

    private static boolean booleanValue(Object raw) {
        if (raw == null) {
            return false;
        }
        if (raw instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (raw instanceof Number numeric) {
            return numeric.intValue() != 0;
        }
        String value = raw.toString().trim().toLowerCase();
        return "true".equals(value) || "1".equals(value) || "yes".equals(value) || "y".equals(value);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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

    private static Map<String, Object> aggregateTelemetry(
            String projectId,
            String executionId,
            List<Map<String, Object>> events,
            String from,
            String to,
            String nodeId,
            String type,
            String groupBy,
            String sort,
            int limit,
            boolean includeRaw) {
        final Map<String, Object> counters = new LinkedHashMap<>();
        counters.put("tasksExecuted", 0L);
        counters.put("delegationAttempts", 0L);
        counters.put("delegationRetries", 0L);
        counters.put("delegationFailures", 0L);
        counters.put("delegationTimeouts", 0L);

        final Set<String> orchestrationTypes = new HashSet<>();
        Map<String, Object> latestBudget = Map.of();
        int telemetryEventCount = 0;

        for (Map<String, Object> event : events) {
            final Map<String, Object> metadata = mapValue(event.get("metadata"));
            final Map<String, Object> telemetry = mapValue(metadata.get("telemetry"));
            if (telemetry.isEmpty()) {
                continue;
            }
            telemetryEventCount++;

            final String orchestrationType = optionalStringValue(telemetry.get("orchestrationType"));
            if (orchestrationType != null) {
                orchestrationTypes.add(orchestrationType);
            }
            counters.put(
                    "tasksExecuted",
                    ((Number) counters.get("tasksExecuted")).longValue()
                            + longValue(telemetry.get("tasksExecuted"), 0L));

            final Map<String, Object> executorTelemetry = mapValue(telemetry.get("executorTelemetry"));
            if (!executorTelemetry.isEmpty()) {
                counters.put(
                        "delegationAttempts",
                        ((Number) counters.get("delegationAttempts")).longValue()
                                + longValue(executorTelemetry.get("delegationAttempts"), 0L));
                counters.put(
                        "delegationRetries",
                        ((Number) counters.get("delegationRetries")).longValue()
                                + longValue(executorTelemetry.get("delegationRetries"), 0L));
                counters.put(
                        "delegationFailures",
                        ((Number) counters.get("delegationFailures")).longValue()
                                + longValue(executorTelemetry.get("delegationFailures"), 0L));
                counters.put(
                        "delegationTimeouts",
                        ((Number) counters.get("delegationTimeouts")).longValue()
                                + longValue(executorTelemetry.get("delegationTimeouts"), 0L));
            }

            final Map<String, Object> budget = mapValue(telemetry.get("budget"));
            if (!budget.isEmpty()) {
                latestBudget = budget;
            }
        }

        final Map<String, Object> response = new LinkedHashMap<>();
        response.put("projectId", projectId);
        response.put("executionId", executionId);
        response.put("eventCount", events.size());
        response.put("telemetryEventCount", telemetryEventCount);
        response.put("orchestrationTypes", orchestrationTypes.stream().sorted().toList());
        Map<String, Object> filters = new LinkedHashMap<>();
        if (optionalStringValue(from) != null) {
            filters.put("from", from);
        }
        if (optionalStringValue(to) != null) {
            filters.put("to", to);
        }
        if (nodeId != null) {
            filters.put("nodeId", nodeId);
        }
        if (type != null) {
            filters.put("type", type);
        }
        if (groupBy != null) {
            filters.put("groupBy", groupBy);
        }
        if (sort != null) {
            filters.put("sort", sort);
        }
        if (limit > 0) {
            filters.put("limit", limit);
        }
        filters.put("includeRaw", includeRaw);
        response.put("filters", filters);
        response.put("counters", counters);
        if (!latestBudget.isEmpty()) {
            response.put("latestBudget", latestBudget);
        }
        if ("nodeId".equalsIgnoreCase(groupBy) || "type".equalsIgnoreCase(groupBy)) {
            List<Map<String, Object>> grouped = "nodeId".equalsIgnoreCase(groupBy)
                    ? aggregateByNode(events)
                    : aggregateByType(events);
            grouped = sortGrouped(grouped, sort);
            if (limit > 0 && grouped.size() > limit) {
                grouped = grouped.subList(0, limit);
            }
            response.put("grouped", grouped);
        }
        if (includeRaw) {
            response.put("rawEventCount", events.size());
            response.put("rawEvents", events);
        }
        response.put("aggregatedAt", Instant.now().toString());
        return response;
    }

    private static List<Map<String, Object>> aggregateByNode(List<Map<String, Object>> events) {
        final Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            if (event == null) {
                continue;
            }
            final String nodeId = optionalStringValue(event.get("nodeId"));
            if (nodeId == null) {
                continue;
            }
            final Map<String, Object> metadata = mapValue(event.get("metadata"));
            final Map<String, Object> telemetry = mapValue(metadata.get("telemetry"));
            if (telemetry.isEmpty()) {
                continue;
            }
            final Map<String, Object> counters = grouped.computeIfAbsent(nodeId, ignored -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("nodeId", nodeId);
                item.put("eventCount", 0L);
                item.put("tasksExecuted", 0L);
                item.put("delegationAttempts", 0L);
                item.put("delegationRetries", 0L);
                item.put("delegationFailures", 0L);
                item.put("delegationTimeouts", 0L);
                return item;
            });
            counters.put("eventCount", ((Number) counters.get("eventCount")).longValue() + 1L);
            counters.put(
                    "tasksExecuted",
                    ((Number) counters.get("tasksExecuted")).longValue()
                            + longValue(telemetry.get("tasksExecuted"), 0L));
            Map<String, Object> execTelemetry = mapValue(telemetry.get("executorTelemetry"));
            counters.put(
                    "delegationAttempts",
                    ((Number) counters.get("delegationAttempts")).longValue()
                            + longValue(execTelemetry.get("delegationAttempts"), 0L));
            counters.put(
                    "delegationRetries",
                    ((Number) counters.get("delegationRetries")).longValue()
                            + longValue(execTelemetry.get("delegationRetries"), 0L));
            counters.put(
                    "delegationFailures",
                    ((Number) counters.get("delegationFailures")).longValue()
                            + longValue(execTelemetry.get("delegationFailures"), 0L));
            counters.put(
                    "delegationTimeouts",
                    ((Number) counters.get("delegationTimeouts")).longValue()
                            + longValue(execTelemetry.get("delegationTimeouts"), 0L));
        }
        return grouped.values().stream()
                .sorted(Comparator.comparing(entry -> String.valueOf(entry.getOrDefault("nodeId", ""))))
                .toList();
    }

    private static List<Map<String, Object>> aggregateByType(List<Map<String, Object>> events) {
        final Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> event : events) {
            if (event == null) {
                continue;
            }
            final String type = optionalStringValue(event.get("type"));
            if (type == null) {
                continue;
            }
            final Map<String, Object> metadata = mapValue(event.get("metadata"));
            final Map<String, Object> telemetry = mapValue(metadata.get("telemetry"));
            if (telemetry.isEmpty()) {
                continue;
            }
            final Map<String, Object> counters = grouped.computeIfAbsent(type, ignored -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", type);
                item.put("eventCount", 0L);
                item.put("tasksExecuted", 0L);
                item.put("delegationAttempts", 0L);
                item.put("delegationRetries", 0L);
                item.put("delegationFailures", 0L);
                item.put("delegationTimeouts", 0L);
                return item;
            });
            counters.put("eventCount", ((Number) counters.get("eventCount")).longValue() + 1L);
            counters.put(
                    "tasksExecuted",
                    ((Number) counters.get("tasksExecuted")).longValue()
                            + longValue(telemetry.get("tasksExecuted"), 0L));
            Map<String, Object> execTelemetry = mapValue(telemetry.get("executorTelemetry"));
            counters.put(
                    "delegationAttempts",
                    ((Number) counters.get("delegationAttempts")).longValue()
                            + longValue(execTelemetry.get("delegationAttempts"), 0L));
            counters.put(
                    "delegationRetries",
                    ((Number) counters.get("delegationRetries")).longValue()
                            + longValue(execTelemetry.get("delegationRetries"), 0L));
            counters.put(
                    "delegationFailures",
                    ((Number) counters.get("delegationFailures")).longValue()
                            + longValue(execTelemetry.get("delegationFailures"), 0L));
            counters.put(
                    "delegationTimeouts",
                    ((Number) counters.get("delegationTimeouts")).longValue()
                            + longValue(execTelemetry.get("delegationTimeouts"), 0L));
        }
        return grouped.values().stream()
                .sorted(Comparator.comparing(entry -> String.valueOf(entry.getOrDefault("type", ""))))
                .toList();
    }

    private static List<Map<String, Object>> sortGrouped(List<Map<String, Object>> grouped, String sort) {
        if (grouped == null || grouped.isEmpty() || sort == null) {
            return grouped;
        }
        String[] parts = sort.split(":", 2);
        String field = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                item -> String.valueOf(item.getOrDefault(field, "")));
        if (isNumericGroupField(field)) {
            comparator = Comparator.comparingLong(item -> longValue(item.get(field), 0L));
        }
        if (desc) {
            comparator = comparator.reversed();
        }
        return grouped.stream().sorted(comparator).toList();
    }

    private static boolean isNumericGroupField(String field) {
        return Set.of(
                "eventCount",
                "tasksExecuted",
                "delegationAttempts",
                "delegationRetries",
                "delegationFailures",
                "delegationTimeouts").contains(field);
    }

    private static Instant parseFilterInstant(String value) {
        final String raw = optionalStringValue(value);
        if (raw == null) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean eventMatchesFilter(
            Map<String, Object> event,
            Instant from,
            Instant to,
            String nodeId,
            String type) {
        if (event == null) {
            return false;
        }
        final Instant createdAt = parseInstantOrEpoch(event.get("createdAt"));
        if (from != null && createdAt.isBefore(from)) {
            return false;
        }
        if (to != null && createdAt.isAfter(to)) {
            return false;
        }
        if (nodeId != null && !nodeId.equals(String.valueOf(event.getOrDefault("nodeId", "")))) {
            return false;
        }
        if (type != null) {
            final String eventType = String.valueOf(event.getOrDefault("type", ""));
            if (!type.equalsIgnoreCase(eventType)) {
                return false;
            }
        }
        return true;
    }
}
