package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.inject.Instance;
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
import tech.kayys.wayang.security.secrets.core.SecretManager;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class ProjectsExecutionSubmitSupport {
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_STARTED = "STARTED";

    private ProjectsExecutionSubmitSupport() {
    }

    interface DepthResolver {
        int resolve(Map<String, Object> request, Map<String, Object> specPayload);
    }

    interface SpecExpander {
        Map<String, Object> expand(
                Map<String, Object> rawSpecPayload,
                String projectId,
                int maxDepth,
                String tenantId,
                String requesterUserId) throws Exception;
    }

    static Response createExecution(
            String projectId,
            String tenantId,
            String userId,
            String idempotencyKey,
            String xIdempotencyKey,
            String requestId,
            Map<String, Object> request,
            WayangDefinitionService definitionService,
            SchemaValidationService schemaValidationService,
            GamelanWorkflowRunManager workflowRunManager,
            WayangOrchestratorSpi orchestrator,
            Instance<SecretManager> secretManagerInstance,
            ObjectMapper objectMapper,
            ConcurrentHashMap<String, ProjectsExecutionLifecycleSupport.RateLimitWindow> rateLimitWindows,
            AtomicInteger inFlightExecutionSubmits,
            long defaultRateLimitPerMinute,
            int defaultMaxInFlightExecutionSubmits,
            long defaultRetryAfterSeconds,
            long defaultIdempotencyReplayWindowSeconds,
            String defaultTenant,
            DepthResolver depthResolver,
            SpecExpander specExpander,
            String errorExecutionRateLimited,
            String errorExecutionBackpressure) {
        final ProjectsExecutionLifecycleSupport.RateLimitDecision rateLimit = ProjectsExecutionLifecycleSupport
                .consumeRateLimit(tenantId, defaultTenant, defaultRateLimitPerMinute, rateLimitWindows);
        if (!rateLimit.allowed()) {
            return ProjectsExecutionLifecycleSupport.rateLimitedResponse(rateLimit, errorExecutionRateLimited);
        }
        if (!ProjectsExecutionLifecycleSupport.acquireInFlightPermit(
                inFlightExecutionSubmits,
                ProjectsExecutionLifecycleSupport.maxInFlightExecutionSubmits(defaultMaxInFlightExecutionSubmits))) {
            return ProjectsExecutionLifecycleSupport.backpressureResponse(
                    rateLimit,
                    errorExecutionBackpressure,
                    ProjectsExecutionLifecycleSupport.retryAfterSeconds(defaultRetryAfterSeconds),
                    ProjectsExecutionLifecycleSupport.maxInFlightExecutionSubmits(defaultMaxInFlightExecutionSubmits),
                    inFlightExecutionSubmits.get());
        }
        try {
            final List<Map<String, Object>> projects = ProjectsFileStore.readProjects();
            final boolean projectExists = projects.stream()
                    .anyMatch(project -> projectId.equals(String.valueOf(project.get("projectId"))));
            if (!projectExists) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Project not found: " + projectId))
                        .build();
            }

            final Map<String, Object> body = request != null ? request : Map.of();
            final boolean dryRun = ProjectsValueSupport.booleanValue(body.get("dryRun"))
                    || ProjectsValueSupport.booleanValue(body.get("validateOnly"));
            final String requestIdempotencyKey = ProjectsValueSupport.optionalStringValue(body.get("idempotencyKey"));
            final String resolvedIdempotencyKey = ProjectsValueSupport.firstNonBlank(
                    ProjectsValueSupport.optionalStringValue(idempotencyKey),
                    ProjectsValueSupport.optionalStringValue(xIdempotencyKey),
                    requestIdempotencyKey);
            final long idempotencyReplayWindowSeconds = ProjectsExecutionLifecycleSupport.longValue(
                    body.get("idempotencyReplayWindowSeconds"),
                    ProjectsExecutionLifecycleSupport.idempotencyReplayWindowSeconds(
                            defaultIdempotencyReplayWindowSeconds));
            final String resolvedRequestId = ProjectsValueSupport.firstNonBlank(
                    ProjectsValueSupport.optionalStringValue(requestId),
                    ProjectsValueSupport.optionalStringValue(body.get("requestId")),
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

            final Map<String, Object> rawSpecPayload = ProjectsValueSupport.mapValue(rawSpec);
            final int maxSubWorkflowDepth = depthResolver.resolve(body, rawSpecPayload);
            final String requesterUserId = ProjectsValueSupport.firstNonBlank(
                    ProjectsValueSupport.optionalStringValue(userId),
                    ProjectsValueSupport.optionalStringValue(body.get("userId")),
                    ProjectsValueSupport.optionalStringValue(body.get("requestedBy")));
            final Map<String, Object> executionContext = ProjectsExecutionSupport.buildExecutionContext(
                    projectId,
                    resolvedRequestId,
                    body);
            final Map<String, Object> resolvedSpecPayloadRaw = specExpander.expand(
                    rawSpecPayload,
                    projectId,
                    maxSubWorkflowDepth,
                    tenantId,
                    requesterUserId);
            final Map<String, Object> resolvedSpecPayload = ProjectsExecutionSupport.pruneNulls(
                    ProjectsValueSupport.mapValue(resolvedSpecPayloadRaw));
            final Map<String, Object> subWorkflowResolution = ProjectsSubWorkflowSupport.summarizeResolution(
                    resolvedSpecPayload);
            final WayangSpec spec = objectMapper.convertValue(
                    sanitizeSpecForWayangConversion(resolvedSpecPayload),
                    WayangSpec.class);
            final Map<String, Object> specPayloadRaw = objectMapper.convertValue(spec, new TypeReference<Map<String, Object>>() {});
            final Map<String, Object> specPayload = ProjectsExecutionSupport.pruneNulls(specPayloadRaw);
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

            final String definitionName = ProjectsValueSupport.stringValue(body.get("name"), "project-" + projectId + "-spec");
            final String description = ProjectsValueSupport.stringValue(body.get("description"), "");
            final String createdBy = ProjectsValueSupport.stringValue(body.get("createdBy"), "api");
            final Map<String, Object> inputs = new HashMap<>(ProjectsValueSupport.mapValue(body.get("inputs")));
            final Instant queuedAt = Instant.now();
            final SecretManager secretManager = ProjectsExecutionSupport.resolveSecretManager(secretManagerInstance);
            final Map<String, Object> agentConfigCoverage = ProjectsAgentConfigSupport
                    .summarizeAgentConfigCoverage(specPayload, tenantId, secretManager);
            final Map<String, Object> resolvedCredentials = ProjectsAgentConfigSupport
                    .resolveCredentialInputs(specPayload, tenantId, secretManager);

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
                dryRunResponse.put("maxSubWorkflowDepth", maxSubWorkflowDepth);
                dryRunResponse.put("timestamp", Instant.now().toString());
                if (!executionContext.isEmpty()) {
                    dryRunResponse.put("executionContext", executionContext);
                }
                if (!subWorkflowResolution.isEmpty()) {
                    dryRunResponse.put("subWorkflowResolution", subWorkflowResolution);
                }
                if (!agentConfigCoverage.isEmpty()) {
                    dryRunResponse.put("agentConfigCoverage", agentConfigCoverage);
                }
                if (!resolvedCredentials.isEmpty()) {
                    dryRunResponse.put("resolvedCredentialCount", resolvedCredentials.size());
                    dryRunResponse.put("resolvedCredentialNames", resolvedCredentials.keySet().stream().toList());
                }
                return ProjectsExecutionLifecycleSupport.addRateLimitHeaders(Response.ok(dryRunResponse), rateLimit).build();
            }

            if (resolvedIdempotencyKey != null && ProjectsExecutionLifecycleSupport.isIdempotencyEnabled()) {
                final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
                final Instant nowInstant = Instant.now();
                final Map<String, Object> existingExecution = executions.stream()
                        .filter(e -> projectId.equals(String.valueOf(e.get("projectId")))
                                && tenantId.equals(String.valueOf(e.getOrDefault("tenantId", defaultTenant)))
                                && resolvedIdempotencyKey.equals(String.valueOf(e.get("idempotencyKey"))))
                        .filter(e -> ProjectsExecutionLifecycleSupport.isWithinIdempotencyReplayWindow(
                                e, nowInstant, idempotencyReplayWindowSeconds))
                        .max(Comparator.comparing(e -> ProjectsExecutionLifecycleSupport.parseInstantOrEpoch(e.get("createdAt"))))
                        .orElse(null);
                if (existingExecution != null) {
                    final Map<String, Object> replay = new LinkedHashMap<>(existingExecution);
                    replay.put("idempotentReplay", true);
                    replay.putIfAbsent("requestId", resolvedRequestId);
                    replay.put("idempotencyReplayWindowSeconds", idempotencyReplayWindowSeconds);
                    replay.put("idempotencyReplayAgeSeconds",
                            Duration.between(
                                    ProjectsExecutionLifecycleSupport.parseInstantOrEpoch(existingExecution.get("createdAt")),
                                    nowInstant).getSeconds());
                    return ProjectsExecutionLifecycleSupport.addRateLimitHeaders(Response.ok(replay), rateLimit).build();
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
                if (!ProjectsExecutionSupport.isStandalonePersistenceUnavailable(persistenceFailure)) {
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
            execution.put("status", STATUS_STARTED);
            execution.put("version", 1L);
            execution.put("name", definitionName);
            execution.put("description", description);
            execution.put("createdBy", createdBy);
            execution.put("createdAt", now);
            execution.put("queuedAt", queuedAt.toString());
            execution.put("startedAt", startedAt.toString());
            execution.put("queueDurationMs", queueDurationMs);
            execution.put("maxSubWorkflowDepth", maxSubWorkflowDepth);
            if (!executionContext.isEmpty()) {
                execution.put("executionContext", executionContext);
            }
            if (!subWorkflowResolution.isEmpty()) {
                execution.put("subWorkflowResolution", subWorkflowResolution);
            }
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

            final List<Map<String, Object>> executions = ProjectsFileStore.readExecutions();
            executions.removeIf(existing -> finalExecutionId.equals(String.valueOf(existing.get("executionId"))));
            executions.add(execution);
            ProjectsFileStore.writeExecutions(executions);
            final Map<String, Object> startedMeta = new LinkedHashMap<>();
            startedMeta.put("definitionId", definitionIdValue);
            startedMeta.put("workflowDefinitionId", workflowDefinitionId);
            startedMeta.put("name", definitionName);
            startedMeta.put("requestId", resolvedRequestId);
            startedMeta.put("queuedAt", queuedAt.toString());
            startedMeta.put("startedAt", startedAt.toString());
            startedMeta.put("queueDurationMs", queueDurationMs);
            startedMeta.put("maxSubWorkflowDepth", maxSubWorkflowDepth);
            if (!executionContext.isEmpty()) {
                startedMeta.put("executionContext", executionContext);
            }
            if (!subWorkflowResolution.isEmpty()) {
                startedMeta.put("subWorkflowResolution", subWorkflowResolution);
            }
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
            ProjectsFileStore.appendExecutionEvent(
                    projectId,
                    finalExecutionId,
                    "EXECUTION_QUEUED",
                    STATUS_QUEUED,
                    "Execution queued",
                    startedMeta);
            ProjectsFileStore.appendExecutionEvent(
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
            accepted.put("status", STATUS_STARTED);
            accepted.put("queuedAt", queuedAt.toString());
            accepted.put("startedAt", startedAt.toString());
            accepted.put("queueDurationMs", queueDurationMs);
            accepted.put("maxSubWorkflowDepth", maxSubWorkflowDepth);
            if (!executionContext.isEmpty()) {
                accepted.put("executionContext", executionContext);
            }
            if (!subWorkflowResolution.isEmpty()) {
                accepted.put("subWorkflowResolution", subWorkflowResolution);
            }
            if (resolvedIdempotencyKey != null) {
                accepted.put("idempotencyKey", resolvedIdempotencyKey);
                accepted.put("idempotencyReplayWindowSeconds", idempotencyReplayWindowSeconds);
                if (idempotencyReplayWindowSeconds > 0) {
                    accepted.put("idempotencyExpiresAt", startedAt.plusSeconds(idempotencyReplayWindowSeconds).toString());
                }
            }
            return ProjectsExecutionLifecycleSupport.addRateLimitHeaders(Response.accepted(accepted), rateLimit).build();
        } catch (IllegalArgumentException e) {
            return ProjectsExecutionLifecycleSupport.addRateLimitHeaders(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    , rateLimit).build();
        } catch (IllegalStateException e) {
            return ProjectsExecutionLifecycleSupport.addRateLimitHeaders(Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", e.getMessage()))
                    , rateLimit).build();
        } catch (Exception e) {
            final Map<String, Object> payload = ProjectsExecutionSupport.buildExecutionFailurePayload(e);
            final int statusCode = "EXECUTION_WORKFLOW_INVALID".equals(payload.get("errorCode")) ? 400 : 500;
            return ProjectsExecutionLifecycleSupport.addRateLimitHeaders(Response.status(statusCode)
                    .entity(payload), rateLimit).build();
        } finally {
            ProjectsExecutionLifecycleSupport.releaseInFlightPermit(inFlightExecutionSubmits);
        }
    }

    private static Map<String, Object> sanitizeSpecForWayangConversion(Map<String, Object> spec) {
        final Map<String, Object> sanitized = new LinkedHashMap<>(spec);
        final Map<String, Object> workflow = ProjectsValueSupport.mapValue(sanitized.get("workflow"));
        if (!workflow.isEmpty()) {
            workflow.remove("metadata");
            workflow.remove("children");
            sanitized.put("workflow", workflow);
        }
        return sanitized;
    }

}
