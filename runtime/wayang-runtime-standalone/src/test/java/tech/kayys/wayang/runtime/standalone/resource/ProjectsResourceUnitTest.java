package tech.kayys.wayang.runtime.standalone.resource;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.control.domain.WayangDefinition;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;
import tech.kayys.wayang.schema.validator.SchemaReference;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.schema.validator.ValidationResult;
import tech.kayys.wayang.schema.validator.ValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectsResourceUnitTest {

    private String originalUserHome;
    private Path tempHome;
    private ProjectsResource resource;
    private FakeDefinitionService fakeDefinitionService;

    @BeforeEach
    void setUp() throws Exception {
        originalUserHome = System.getProperty("user.home");
        tempHome = Files.createTempDirectory("wayang-standalone-test-home");
        System.setProperty("user.home", tempHome.toString());

        resource = new ProjectsResource();
        fakeDefinitionService = new FakeDefinitionService();
        resource.definitionService = fakeDefinitionService;
        resource.schemaValidationService = new AllowAllSchemaValidationService();
    }

    @AfterEach
    void tearDown() {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void shouldExecuteAndManageProjectExecutionLifecycle() {
        Response created = resource.createProject(Map.of(
                "projectName", "E2E Test Project",
                "description", "test"));
        assertEquals(201, created.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));
        assertNotNull(projectId);

        Response execute = resource.createExecution(projectId, "community", Map.of(
                "name", "unit-test-run",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, execute.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> executionStart = (Map<String, Object>) execute.getEntity();
        String executionId = String.valueOf(executionStart.get("executionId"));
        assertNotNull(executionId);
        assertNotNull(executionStart.get("requestId"));

        Response status = resource.getExecutionStatus(projectId, executionId);
        assertEquals(200, status.getStatus());
        assertNotNull(status.getHeaderString("ETag"));

        @SuppressWarnings("unchecked")
        Map<String, Object> statusPayload = (Map<String, Object>) status.getEntity();
        assertEquals("RUNNING", String.valueOf(statusPayload.get("status")));

        Response stop = resource.stopExecution(projectId, executionId);
        assertEquals(200, stop.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> stopPayload = (Map<String, Object>) stop.getEntity();
        assertEquals("STOPPED", String.valueOf(stopPayload.get("status")));

        Response list = resource.listExecutions(projectId);
        assertEquals(200, list.getStatus());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> listPayload = (List<Map<String, Object>>) list.getEntity();
        assertEquals(1, listPayload.size());
    }

    @Test
    void shouldReturnNotModifiedWhenExecutionEtagMatches() {
        Response created = resource.createProject(Map.of(
                "projectName", "ETag Project",
                "description", "conditional get test"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response execute = resource.createExecution(projectId, "community", Map.of(
                "name", "etag-run",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, execute.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> startPayload = (Map<String, Object>) execute.getEntity();
        String executionId = String.valueOf(startPayload.get("executionId"));

        Response first = resource.getExecutionStatus(projectId, executionId, null);
        assertEquals(200, first.getStatus());
        String etag = first.getHeaderString("ETag");
        assertNotNull(etag);

        Response second = resource.getExecutionStatus(projectId, executionId, etag);
        assertEquals(304, second.getStatus());
    }

    @Test
    void shouldRejectExecutionRequestWithoutSpec() {
        Response created = resource.createProject(Map.of("projectName", "No-Spec Project"));
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response execute = resource.createExecution(projectId, "community", Map.of("name", "bad-request"));
        assertEquals(400, execute.getStatus());
    }

    @Test
    void shouldExecuteTriggerBasedWayangSpec() {
        Response created = resource.createProject(Map.of("projectName", "Trigger Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Map<String, Object> triggerSpec = Map.of(
                "specVersion", "1.0.0",
                "canvas", Map.of(
                        "nodes", List.of(
                                Map.of(
                                        "id", "node-trigger",
                                        "type", "trigger-schedule",
                                        "label", "Schedule Trigger",
                                        "config", Map.of(
                                                "mode", "interval",
                                                "intervalSeconds", 60,
                                                "timezone", "Asia/Jakarta"))),
                        "edges", List.of()));

        Response execute = resource.createExecution(projectId, "community", Map.of(
                "name", "trigger-run",
                "spec", triggerSpec));
        assertEquals(202, execute.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> executePayload = (Map<String, Object>) execute.getEntity();
        String executionId = String.valueOf(executePayload.get("executionId"));
        assertNotNull(executionId);

        Response eventsResponse = resource.listExecutionEvents(projectId, executionId);
        assertEquals(200, eventsResponse.getStatus());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) eventsResponse.getEntity();
        assertEquals(2, events.size());
        assertEquals("EXECUTION_QUEUED", String.valueOf(events.get(0).get("type")));
        assertEquals("EXECUTION_STARTED", String.valueOf(events.get(1).get("type")));
    }

    @Test
    void shouldValidateWithoutExecutingWhenDryRunEnabled() {
        Response created = resource.createProject(Map.of("projectName", "Dry Run Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response execute = resource.createExecution(projectId, "community", Map.of(
                "name", "dry-run-check",
                "dryRun", true,
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(200, execute.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
        assertEquals("DRY_RUN_VALID", String.valueOf(payload.get("status")));
        assertNotNull(payload.get("requestId"));
        assertEquals(Boolean.TRUE, payload.get("dryRun"));
        assertEquals(Boolean.TRUE, payload.get("canExecute"));

        Response listExecutions = resource.listExecutions(projectId);
        assertEquals(200, listExecutions.getStatus());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> executions = (List<Map<String, Object>>) listExecutions.getEntity();
        assertTrue(executions.isEmpty(), "dry-run must not create an execution record");

        assertEquals(0, fakeDefinitionService.createCalls.get(), "dry-run must not call create()");
        assertEquals(0, fakeDefinitionService.publishCalls.get(), "dry-run must not call publish()");
        assertEquals(0, fakeDefinitionService.runCalls.get(), "dry-run must not call run()");
    }

    @Test
    void shouldReturnExistingExecutionForDuplicateIdempotencyKey() {
        Response created = resource.createProject(Map.of("projectName", "Idempotency Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response first = resource.createExecution(projectId, "community", "idem-1", null, null, Map.of(
                "name", "idempotent-run",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, first.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> firstPayload = (Map<String, Object>) first.getEntity();
        String firstExecutionId = String.valueOf(firstPayload.get("executionId"));
        assertNotNull(firstExecutionId);

        Response replay = resource.createExecution(projectId, "community", "idem-1", null, null, Map.of(
                "name", "idempotent-run-retry",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(200, replay.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> replayPayload = (Map<String, Object>) replay.getEntity();
        assertEquals(firstExecutionId, String.valueOf(replayPayload.get("executionId")));
        assertEquals(Boolean.TRUE, replayPayload.get("idempotentReplay"));

        assertEquals(1, fakeDefinitionService.createCalls.get(), "duplicate idempotent submit must not create");
        assertEquals(1, fakeDefinitionService.publishCalls.get(), "duplicate idempotent submit must not publish");
        assertEquals(1, fakeDefinitionService.runCalls.get(), "duplicate idempotent submit must not run");
    }

    @Test
    void shouldCreateNewExecutionWhenIdempotencyReplayWindowIsDisabled() {
        Response created = resource.createProject(Map.of("projectName", "Idempotency Replay Window Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response first = resource.createExecution(projectId, "community", "idem-window-1", null, null, Map.of(
                "name", "idempotent-run",
                "idempotencyReplayWindowSeconds", 0,
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, first.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> firstPayload = (Map<String, Object>) first.getEntity();
        String firstExecutionId = String.valueOf(firstPayload.get("executionId"));

        Response second = resource.createExecution(projectId, "community", "idem-window-1", null, null, Map.of(
                "name", "idempotent-run-second",
                "idempotencyReplayWindowSeconds", 0,
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, second.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> secondPayload = (Map<String, Object>) second.getEntity();
        String secondExecutionId = String.valueOf(secondPayload.get("executionId"));

        assertTrue(!firstExecutionId.equals(secondExecutionId), "replay disabled should create a new execution");
        assertEquals(2, fakeDefinitionService.createCalls.get(), "disabled replay should allow second create");
        assertEquals(2, fakeDefinitionService.publishCalls.get(), "disabled replay should allow second publish");
        assertEquals(2, fakeDefinitionService.runCalls.get(), "disabled replay should allow second run");
    }

    @Test
    void shouldRejectInvalidLifecycleTransitions() {
        Response created = resource.createProject(Map.of("projectName", "Lifecycle Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response first = resource.createExecution(projectId, "community", Map.of(
                "name", "lifecycle-run",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, first.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> firstPayload = (Map<String, Object>) first.getEntity();
        String executionId = String.valueOf(firstPayload.get("executionId"));
        assertNotNull(executionId);

        Response stop = resource.stopExecution(projectId, executionId);
        assertEquals(200, stop.getStatus());

        Response stopAgain = resource.stopExecution(projectId, executionId);
        assertEquals(409, stopAgain.getStatus());

        Response resumeStopped = resource.resumeExecution(projectId, executionId, Map.of());
        assertEquals(409, resumeStopped.getStatus());
    }

    @Test
    void shouldPersistStopReasonAndNote() {
        Response created = resource.createProject(Map.of("projectName", "Stop Reason Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response execute = resource.createExecution(projectId, "community", Map.of(
                "name", "stop-reason-run",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, execute.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> executionStart = (Map<String, Object>) execute.getEntity();
        String executionId = String.valueOf(executionStart.get("executionId"));

        Response stop = resource.stopExecution(projectId, executionId, Map.of(
                "reason", "manual_intervention",
                "note", "operator initiated stop"));
        assertEquals(200, stop.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> stopPayload = (Map<String, Object>) stop.getEntity();
        assertEquals("MANUAL_INTERVENTION", String.valueOf(stopPayload.get("stopReason")));
        assertEquals("operator initiated stop", String.valueOf(stopPayload.get("stopNote")));

        Response eventsResponse = resource.listExecutionEvents(projectId, executionId);
        assertEquals(200, eventsResponse.getStatus());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> events = (List<Map<String, Object>>) eventsResponse.getEntity();
        Map<String, Object> lastEvent = events.get(events.size() - 1);
        assertEquals("EXECUTION_STOPPED", String.valueOf(lastEvent.get("type")));
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) lastEvent.get("metadata");
        assertEquals("MANUAL_INTERVENTION", String.valueOf(metadata.get("reason")));
    }

    @Test
    void shouldRejectStopWhenIfMatchVersionMismatches() {
        Response created = resource.createProject(Map.of("projectName", "Version Conflict Project"));
        assertEquals(201, created.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> project = (Map<String, Object>) created.getEntity();
        String projectId = String.valueOf(project.get("projectId"));

        Response execute = resource.createExecution(projectId, "community", Map.of(
                "name", "version-check-run",
                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
        assertEquals(202, execute.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> executionStart = (Map<String, Object>) execute.getEntity();
        String executionId = String.valueOf(executionStart.get("executionId"));

        Response stopConflict = resource.stopExecution(projectId, executionId, "99", Map.of());
        assertEquals(409, stopConflict.getStatus());
        assertNotNull(stopConflict.getHeaderString("Retry-After"));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) stopConflict.getEntity();
        assertEquals("EXECUTION_VERSION_CONFLICT", String.valueOf(payload.get("errorCode")));
        assertEquals(Boolean.TRUE, payload.get("retryable"));
        assertNotNull(payload.get("retryAfterSeconds"));
    }

    @Test
    void shouldExposeRateLimitHeadersAndRejectWhenLimitExceeded() {
        String originalLimit = System.getProperty("wayang.runtime.standalone.execution.rate-limit.per-minute");
        String originalEnabled = System.getProperty("wayang.runtime.standalone.execution.rate-limit.enabled");
        System.setProperty("wayang.runtime.standalone.execution.rate-limit.enabled", "true");
        System.setProperty("wayang.runtime.standalone.execution.rate-limit.per-minute", "1");
        try {
            Response created = resource.createProject(Map.of("projectName", "Rate Limit Project"));
            assertEquals(201, created.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> project = (Map<String, Object>) created.getEntity();
            String projectId = String.valueOf(project.get("projectId"));
            String tenantId = "tenant-rate-limit-" + UUID.randomUUID();

            Response first = resource.createExecution(projectId, tenantId, Map.of(
                    "name", "rate-limit-run-1",
                    "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
            assertEquals(202, first.getStatus());
            assertNotNull(first.getHeaderString("X-RateLimit-Limit"));
            assertNotNull(first.getHeaderString("X-RateLimit-Remaining"));
            assertNotNull(first.getHeaderString("X-RateLimit-Reset"));

            Response second = resource.createExecution(projectId, tenantId, Map.of(
                    "name", "rate-limit-run-2",
                    "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
            assertEquals(429, second.getStatus());
            assertNotNull(second.getHeaderString("X-RateLimit-Limit"));
            assertNotNull(second.getHeaderString("Retry-After"));
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) second.getEntity();
            assertEquals("EXECUTION_RATE_LIMITED", String.valueOf(payload.get("errorCode")));
            assertEquals(Boolean.TRUE, payload.get("retryable"));
        } finally {
            if (originalEnabled == null) {
                System.clearProperty("wayang.runtime.standalone.execution.rate-limit.enabled");
            } else {
                System.setProperty("wayang.runtime.standalone.execution.rate-limit.enabled", originalEnabled);
            }
            if (originalLimit == null) {
                System.clearProperty("wayang.runtime.standalone.execution.rate-limit.per-minute");
            } else {
                System.setProperty("wayang.runtime.standalone.execution.rate-limit.per-minute", originalLimit);
            }
        }
    }

    private static final class AllowAllSchemaValidationService implements SchemaValidationService {
        @Override
        public ValidationResult validateSchema(String schema, Map<String, Object> data) {
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateSchema(SchemaReference schemaRef, Map<String, Object> data) {
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateWithRules(ValidationRule[] rules, Map<String, Object> data) {
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateComprehensive(String schema, ValidationRule[] rules, Map<String, Object> data) {
            return ValidationResult.success();
        }
    }

    private static final class FakeDefinitionService extends WayangDefinitionService {
        private final AtomicInteger createCalls = new AtomicInteger();
        private final AtomicInteger publishCalls = new AtomicInteger();
        private final AtomicInteger runCalls = new AtomicInteger();

        @Override
        public Uni<WayangDefinition> create(String tenantId, UUID projectId, String name,
                String description, DefinitionType type, WayangSpec spec, String createdBy) {
            createCalls.incrementAndGet();
            WayangDefinition definition = new WayangDefinition();
            definition.definitionId = UUID.randomUUID();
            definition.tenantId = tenantId;
            definition.projectId = projectId;
            definition.name = name;
            definition.description = description;
            definition.definitionType = type;
            definition.spec = spec;
            definition.workflowDefinitionId = "wf-" + definition.definitionId;
            definition.createdBy = createdBy;
            definition.createdAt = Instant.now();
            return Uni.createFrom().item(definition);
        }

        @Override
        public Uni<WayangDefinition> publish(UUID definitionId, String publishedBy) {
            publishCalls.incrementAndGet();
            WayangDefinition definition = new WayangDefinition();
            definition.definitionId = definitionId;
            definition.workflowDefinitionId = "wf-" + definitionId;
            return Uni.createFrom().item(definition);
        }

        @Override
        public Uni<String> run(UUID definitionId, Map<String, Object> inputs) {
            runCalls.incrementAndGet();
            return Uni.createFrom().item("run-" + definitionId);
        }

        @Override
        public Uni<String> getExecutionStatus(String executionId) {
            return Uni.createFrom().item("RUNNING");
        }

        @Override
        public Uni<Boolean> stopExecution(String executionId) {
            return Uni.createFrom().item(true);
        }
    }
}
