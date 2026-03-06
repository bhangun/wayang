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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectsResourceUnitTest {

    private String originalUserHome;
    private Path tempHome;
    private ProjectsResource resource;

    @BeforeEach
    void setUp() throws Exception {
        originalUserHome = System.getProperty("user.home");
        tempHome = Files.createTempDirectory("wayang-standalone-test-home");
        System.setProperty("user.home", tempHome.toString());

        resource = new ProjectsResource();
        resource.definitionService = new FakeDefinitionService();
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

        Response status = resource.getExecutionStatus(projectId, executionId);
        assertEquals(200, status.getStatus());

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
        assertEquals(1, events.size());
        assertEquals("EXECUTION_STARTED", String.valueOf(events.get(0).get("type")));
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
        @Override
        public Uni<WayangDefinition> create(String tenantId, UUID projectId, String name,
                String description, DefinitionType type, WayangSpec spec, String createdBy) {
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
            WayangDefinition definition = new WayangDefinition();
            definition.definitionId = definitionId;
            definition.workflowDefinitionId = "wf-" + definitionId;
            return Uni.createFrom().item(definition);
        }

        @Override
        public Uni<String> run(UUID definitionId, Map<String, Object> inputs) {
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
