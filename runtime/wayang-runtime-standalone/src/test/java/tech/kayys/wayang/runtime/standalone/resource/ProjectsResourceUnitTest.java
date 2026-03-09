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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectsResourceUnitTest {

        private String originalUserHome;
        private Path tempHome;
        private ProjectsService resource;
        private FakeDefinitionService fakeDefinitionService;

        @BeforeEach
        void setUp() throws Exception {
                originalUserHome = System.getProperty("user.home");
                tempHome = Files.createTempDirectory("wayang-standalone-test-home");
                System.setProperty("user.home", tempHome.toString());

                resource = new ProjectsService();
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
                                                                                                "timezone",
                                                                                                "Asia/Jakarta"))),
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
        void shouldExpandInlineSubWorkflowNodeIntoParentWorkflow() {
                Response created = resource.createProject(Map.of("projectName", "Subworkflow Parent"));
                assertEquals(201, created.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> project = (Map<String, Object>) created.getEntity();
                String projectId = String.valueOf(project.get("projectId"));

                Map<String, Object> childSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "child-a"),
                                                                                "type", "agent-basic",
                                                                                "configuration",
                                                                                Map.of("goal", "do work"))),
                                                "connections", List.of()));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of("metadata", Map.of("id", "start"), "type",
                                                                                "trigger-manual", "configuration",
                                                                                Map.of()),
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom-1"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("wayangSpec", childSpec)),
                                                                Map.of("metadata", Map.of("id", "end"), "type",
                                                                                "agent-evaluator", "configuration",
                                                                                Map.of())),
                                                "connections", List.of(
                                                                Map.of("fromNodeId", "start", "toNodeId", "custom-1"),
                                                                Map.of("fromNodeId", "custom-1", "toNodeId", "end"))));

                Response execute = resource.createExecution(projectId, "community", Map.of(
                                "name", "subworkflow-inline",
                                "spec", parentSpec));
                assertEquals(202, execute.getStatus());

                WayangSpec persisted = fakeDefinitionService.lastCreatedSpec;
                assertNotNull(persisted);
                assertNotNull(persisted.getWorkflow());
                List<?> nodes = persisted.getWorkflow().getNodes();
                assertNotNull(nodes);
                assertFalse(nodes.isEmpty());
                assertTrue(nodes.stream().noneMatch(
                                node -> "sub-workflow".equals(((tech.kayys.wayang.schema.node.Node) node).getType())));
                assertTrue(nodes.stream().anyMatch(
                                node -> "agent-basic".equals(((tech.kayys.wayang.schema.node.Node) node).getType())));
        }

        @Test
        void shouldRejectSubWorkflowWhenDepthLimitExceeded() {
                Response created = resource.createProject(Map.of("projectName", "Subworkflow Depth"));
                assertEquals(201, created.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> project = (Map<String, Object>) created.getEntity();
                String projectId = String.valueOf(project.get("projectId"));

                Map<String, Object> level4 = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes",
                                                List.of(Map.of("metadata", Map.of("id", "n4"), "type", "agent-basic",
                                                                "configuration", Map.of())),
                                                "connections", List.of()));

                Map<String, Object> level3 = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "n3"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("wayangSpec", level4))),
                                                "connections", List.of()));

                Map<String, Object> level2 = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "n2"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("wayangSpec", level3))),
                                                "connections", List.of()));

                Map<String, Object> level1 = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "n1"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("wayangSpec", level2))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(projectId, "community", Map.of(
                                "name", "subworkflow-depth",
                                "maxSubWorkflowDepth", 2,
                                "spec", level1));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertTrue(String.valueOf(payload.get("message")).contains("depth limit"));
        }

        @Test
        void shouldExpandProjectReferencedSubWorkflowNode() {
                Response childCreated = resource.createProject(Map.of(
                                "projectName", "Child Project",
                                "metadata", Map.of(
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of(
                                                                                                                "metadata",
                                                                                                                Map.of("id", "child-agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of("goal", "from child project"))),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent Project"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of("metadata", Map.of("id", "start"), "type",
                                                                                "trigger-manual", "configuration",
                                                                                Map.of()),
                                                                Map.of(
                                                                                "metadata",
                                                                                Map.of("id", "custom-child"),
                                                                                "type", "custom-agent-node",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId)),
                                                                Map.of("metadata", Map.of("id", "end"), "type",
                                                                                "agent-evaluator", "configuration",
                                                                                Map.of())),
                                                "connections", List.of(
                                                                Map.of("fromNodeId", "start", "toNodeId",
                                                                                "custom-child"),
                                                                Map.of("fromNodeId", "custom-child", "toNodeId",
                                                                                "end"))));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "subworkflow-project-ref",
                                "spec", parentSpec));
                assertEquals(202, execute.getStatus());

                WayangSpec persisted = fakeDefinitionService.lastCreatedSpec;
                assertNotNull(persisted);
                assertNotNull(persisted.getWorkflow());
                assertTrue(persisted.getWorkflow().getNodes().stream()
                                .anyMatch(node -> "agent-basic".equals(node.getType())));
                assertTrue(persisted.getWorkflow().getNodes().stream()
                                .noneMatch(node -> "custom-agent-node".equals(node.getType())));
        }

        @Test
        void shouldRejectSubWorkflowProjectCycle() {
                Response projectCreated = resource.createProject(Map.of("projectName", "Cycle Project"));
                assertEquals(201, projectCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> project = (Map<String, Object>) projectCreated.getEntity();
                String projectId = String.valueOf(project.get("projectId"));

                Map<String, Object> selfSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "self"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("projectId", projectId))),
                                                "connections", List.of()));

                Response updated = resource.updateProject(projectId, Map.of(
                                "projectName", "Cycle Project",
                                "metadata", Map.of("wayangSpec", selfSpec)));
                assertEquals(200, updated.getStatus());

                Response execute = resource.createExecution(projectId, "community", Map.of(
                                "name", "cycle-test",
                                "spec", selfSpec));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertTrue(String.valueOf(payload.get("message")).toLowerCase().contains("cycle"));
        }

        @Test
        void shouldRejectSubWorkflowWhenReferencedProjectDoesNotExist() {
                Response parentCreated = resource.createProject(Map.of("projectName", "Parent Missing Child"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> spec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata",
                                                                                Map.of("id", "missing-child"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("projectId",
                                                                                                "00000000-0000-0000-0000-000000000000"))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "missing-child",
                                "spec", spec));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertTrue(String.valueOf(payload.get("message")).contains("Sub-workflow project not found"));
        }

        @Test
        void shouldExposeSubWorkflowResolutionSummaryInDryRun() {
                Response childCreated = resource.createProject(Map.of(
                                "projectName", "Child For DryRun",
                                "metadata", Map.of(
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of(
                                                                                                                "metadata",
                                                                                                                Map.of("id", "child-agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent DryRun"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> spec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom"),
                                                                                "type", "custom-agent-node",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "dryrun-subworkflow-summary",
                                "dryRun", true,
                                "spec", spec));
                assertEquals(200, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                @SuppressWarnings("unchecked")
                Map<String, Object> summary = (Map<String, Object>) payload.get("subWorkflowResolution");
                assertNotNull(summary);
                assertEquals(1, ((Number) summary.get("childReferences")).intValue());
                assertEquals(1, ((Number) summary.get("childrenResolved")).intValue());
                assertTrue(((Number) summary.get("expandedNodeCount")).longValue() >= 1L);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trace = (List<Map<String, Object>>) summary.get("trace");
                assertNotNull(trace);
                assertEquals(1, trace.size());
                Map<String, Object> firstTrace = trace.get(0);
                assertEquals("custom", String.valueOf(firstTrace.get("parentNodeId")));
                assertEquals(parentProjectId, String.valueOf(firstTrace.get("parentProjectId")));
                @SuppressWarnings("unchecked")
                Map<String, Object> bindingSummary = (Map<String, Object>) firstTrace.get("bindingSummary");
                assertNotNull(bindingSummary);
                assertEquals(0, ((Number) bindingSummary.get("inputCount")).intValue());
                assertEquals(0, ((Number) bindingSummary.get("outputBindingCount")).intValue());
        }

        @Test
        void shouldExposeExecutionLineageEndpointForSubWorkflowExecution() {
                Response childCreated = resource.createProject(Map.of(
                                "projectName", "Child For Lineage",
                                "metadata", Map.of(
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of(
                                                                                                                "metadata",
                                                                                                                Map.of("id", "child-agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent For Lineage"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> spec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom"),
                                                                                "type", "custom-agent-node",
                                                                                "configuration", Map.of(
                                                                                                "projectId",
                                                                                                childProjectId,
                                                                                                "inputs",
                                                                                                Map.of("ticketId",
                                                                                                                "INC-77")))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "lineage-subworkflow-run",
                                "spec", spec));
                assertEquals(202, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> executionPayload = (Map<String, Object>) execute.getEntity();
                String executionId = String.valueOf(executionPayload.get("executionId"));
                assertNotNull(executionId);

                Response lineage = resource.getExecutionLineage(parentProjectId, executionId);
                assertEquals(200, lineage.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> lineagePayload = (Map<String, Object>) lineage.getEntity();
                assertEquals(parentProjectId, String.valueOf(lineagePayload.get("projectId")));
                assertEquals(executionId, String.valueOf(lineagePayload.get("executionId")));
                assertEquals(List.of("executionContext", "subWorkflowResolution", "status", "updatedAt"),
                                lineagePayload.get("include"));
                assertEquals(List.of(), lineagePayload.get("ignoredIncludes"));
                assertEquals(1, ((Number) lineagePayload.get("traceCount")).intValue());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trace = (List<Map<String, Object>>) lineagePayload.get("trace");
                assertEquals(1, trace.size());
                assertEquals("custom", String.valueOf(trace.get(0).get("parentNodeId")));

                Response compactFiltered = resource.getExecutionLineage(
                                parentProjectId, executionId, "compact", "custom", null, null, null, null, null);
                assertEquals(200, compactFiltered.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> compactPayload = (Map<String, Object>) compactFiltered.getEntity();
                assertEquals("compact", String.valueOf(compactPayload.get("view")));
                assertEquals("custom", String.valueOf(compactPayload.get("nodeId")));
                assertEquals(List.of("executionContext"), compactPayload.get("include"));
                assertEquals(1, ((Number) compactPayload.get("traceCount")).intValue());
                assertEquals(1, ((Number) compactPayload.get("totalTraceCount")).intValue());
                assertFalse(compactPayload.containsKey("subWorkflowResolution"));

                Response projected = resource.getExecutionLineage(
                                parentProjectId, executionId, "compact", "custom", null, null, null,
                                "childId,parentNodeId", null);
                assertEquals(200, projected.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> projectedPayload = (Map<String, Object>) projected.getEntity();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> projectedTrace = (List<Map<String, Object>>) projectedPayload.get("trace");
                assertEquals(1, projectedTrace.size());
                assertEquals(List.of("childId", "parentNodeId"), projectedPayload.get("fields"));
                assertEquals(List.of(), projectedPayload.get("ignoredFields"));
                assertEquals(2, projectedTrace.get(0).size());
                assertEquals("custom", String.valueOf(projectedTrace.get(0).get("childId")));
                assertEquals("custom", String.valueOf(projectedTrace.get(0).get("parentNodeId")));

                Response projectedWithIgnored = resource.getExecutionLineage(
                                parentProjectId, executionId, "compact", "custom", null, null, null,
                                "parentNodeId,unknown,childId", null);
                assertEquals(200, projectedWithIgnored.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> projectedWithIgnoredPayload = (Map<String, Object>) projectedWithIgnored
                                .getEntity();
                assertEquals(List.of("childId", "parentNodeId"), projectedWithIgnoredPayload.get("fields"));
                assertEquals(List.of("unknown"), projectedWithIgnoredPayload.get("ignoredFields"));

                Response includeSelected = resource.getExecutionLineage(
                                parentProjectId, executionId, "compact", "custom", null, null, null, null,
                                "updatedAt,status,unknown");
                assertEquals(200, includeSelected.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> includePayload = (Map<String, Object>) includeSelected.getEntity();
                assertEquals(List.of("status", "updatedAt"), includePayload.get("include"));
                assertEquals(List.of("unknown"), includePayload.get("ignoredIncludes"));
                assertTrue(includePayload.containsKey("status"));
                assertTrue(includePayload.containsKey("updatedAt"));
                assertFalse(includePayload.containsKey("executionContext"));
        }

        @Test
        void shouldApplyLineageSortAndPagination() {
                Response childCreated = resource.createProject(Map.of(
                                "projectName", "Child For Pagination",
                                "metadata", Map.of(
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of(
                                                                                                                "metadata",
                                                                                                                Map.of("id", "child-agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent For Pagination"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> spec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom-a"),
                                                                                "type", "custom-agent-node",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId)),
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom-b"),
                                                                                "type", "custom-agent-node",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "lineage-pagination-run",
                                "spec", spec));
                assertEquals(202, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> executionPayload = (Map<String, Object>) execute.getEntity();
                String executionId = String.valueOf(executionPayload.get("executionId"));

                Response lineage = resource.getExecutionLineage(
                                parentProjectId,
                                executionId,
                                "compact",
                                null,
                                "childId:asc",
                                1,
                                1,
                                null,
                                null);
                assertEquals(200, lineage.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) lineage.getEntity();
                assertEquals(2, ((Number) payload.get("totalTraceCount")).intValue());
                assertEquals(2, ((Number) payload.get("filteredTraceCount")).intValue());
                assertEquals(1, ((Number) payload.get("traceCount")).intValue());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trace = (List<Map<String, Object>>) payload.get("trace");
                assertEquals(1, trace.size());
                assertEquals("custom-b", String.valueOf(trace.get(0).get("childId")));
        }

        @Test
        void shouldDenyCrossTenantSubWorkflowWhenNotShared() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "tenant-a",
                                "createdBy", "owner-a",
                                "projectName", "Private Child",
                                "metadata", Map.of(
                                                "access", Map.of(
                                                                "ownerTenantId", "tenant-a",
                                                                "ownerUserId", "owner-a",
                                                                "visibility", "private"),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "child"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of(
                                "tenantId", "tenant-b",
                                "createdBy", "owner-b",
                                "projectName", "Parent B"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(
                                parentProjectId,
                                "tenant-b",
                                "owner-b",
                                null,
                                null,
                                null,
                                Map.of("name", "cross-tenant-deny", "spec", parentSpec));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertTrue(String.valueOf(payload.get("message")).contains("Access denied"));
        }

        @Test
        void shouldAllowCrossTenantSubWorkflowWhenSharedByTenant() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "tenant-a",
                                "createdBy", "owner-a",
                                "projectName", "Shared Child",
                                "metadata", Map.of(
                                                "access", Map.of(
                                                                "ownerTenantId", "tenant-a",
                                                                "ownerUserId", "owner-a",
                                                                "visibility", "explicit",
                                                                "sharedWithTenants", List.of("tenant-b")),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "child"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of(
                                "tenantId", "tenant-b",
                                "createdBy", "owner-b",
                                "projectName", "Parent B"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "custom"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(
                                parentProjectId,
                                "tenant-b",
                                "owner-b",
                                null,
                                null,
                                null,
                                Map.of("name", "cross-tenant-allow", "spec", parentSpec));
                assertEquals(202, execute.getStatus());
        }

        @Test
        void shouldListOnlyCallableShareableProjectsWithContract() {
                Response callable = resource.createProject(Map.of(
                                "tenantId", "tenant-a",
                                "createdBy", "owner-a",
                                "projectName", "Callable Child",
                                "metadata", Map.of(
                                                "access", Map.of(
                                                                "ownerTenantId", "tenant-a",
                                                                "ownerUserId", "owner-a",
                                                                "visibility", "explicit",
                                                                "sharedWithTenants", List.of("tenant-b")),
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "parameterized"),
                                                                "contract", Map.of(
                                                                                "inputs", Map.of(
                                                                                                "required",
                                                                                                List.of(Map.of("name",
                                                                                                                "ticketId",
                                                                                                                "type",
                                                                                                                "string")),
                                                                                                "optional",
                                                                                                List.of(Map.of("name",
                                                                                                                "priority",
                                                                                                                "type",
                                                                                                                "number"))),
                                                                                "output", Map.of(
                                                                                                "type", "object",
                                                                                                "properties",
                                                                                                Map.of("summary", Map
                                                                                                                .of("type", "string")),
                                                                                                "required",
                                                                                                List.of("summary")))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-parameterized",
                                                                                                                "configuration",
                                                                                                                Map.of()),
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections",
                                                                                List.of(Map.of("fromNodeId", "start",
                                                                                                "toNodeId",
                                                                                                "agent")))))));
                assertEquals(201, callable.getStatus());

                Response autonomous = resource.createProject(Map.of(
                                "tenantId", "tenant-a",
                                "createdBy", "owner-a",
                                "projectName", "Autonomous Child",
                                "metadata", Map.of(
                                                "access", Map.of(
                                                                "ownerTenantId", "tenant-a",
                                                                "ownerUserId", "owner-a",
                                                                "visibility", "explicit",
                                                                "sharedWithTenants", List.of("tenant-b")),
                                                "reuse", Map.of("enabled", true, "mode", "autonomous"),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-schedule",
                                                                                                                "configuration",
                                                                                                                Map.of("intervalSeconds",
                                                                                                                                60))),
                                                                                "connections", List.of())))));
                assertEquals(201, autonomous.getStatus());

                Response response = resource.listShareableProjects("tenant-b", "bob", "callable", null);
                assertEquals(200, response.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) response.getEntity();
                assertEquals("callable", String.valueOf(payload.get("mode")));
                assertEquals(1, ((Number) payload.get("count")).intValue());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> projects = (List<Map<String, Object>>) payload.get("projects");
                assertEquals(1, projects.size());
                @SuppressWarnings("unchecked")
                Map<String, Object> callableContract = (Map<String, Object>) projects.get(0).get("callable");
                assertEquals("callable", String.valueOf(callableContract.get("mode")));
                @SuppressWarnings("unchecked")
                Map<String, Object> inputs = (Map<String, Object>) callableContract.get("inputs");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> required = (List<Map<String, Object>>) inputs.get("required");
                assertEquals("ticketId", String.valueOf(required.get(0).get("name")));
                @SuppressWarnings("unchecked")
                Map<String, Object> output = (Map<String, Object>) callableContract.get("output");
                assertEquals("object", String.valueOf(output.get("type")));
        }

        @Test
        void shouldRejectAutonomousSubWorkflowReference() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Autonomous Child",
                                "metadata", Map.of(
                                                "reuse", Map.of("enabled", true, "mode", "autonomous"),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-schedule",
                                                                                                                "configuration",
                                                                                                                Map.of("intervalSeconds",
                                                                                                                                60))),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent Project"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata",
                                                                                Map.of("id", "custom-child"),
                                                                                "type", "custom-agent-node",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "reject-autonomous-child",
                                "spec", parentSpec));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertTrue(String.valueOf(payload.get("message")).contains("not callable"));
        }

        @Test
        void shouldRejectParameterizedSubWorkflowWhenRequiredInputMissing() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Parameterized Child",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "parameterized"),
                                                                "contract", Map.of(
                                                                                "inputs", Map.of(
                                                                                                "required",
                                                                                                List.of(Map.of("name",
                                                                                                                "ticketId",
                                                                                                                "type",
                                                                                                                "string"))))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-parameterized",
                                                                                                                "configuration",
                                                                                                                Map.of()),
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections",
                                                                                List.of(Map.of("fromNodeId", "start",
                                                                                                "toNodeId",
                                                                                                "agent")))))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata",
                                                                                Map.of("id", "custom-child"),
                                                                                "type", "sub-workflow",
                                                                                "configuration",
                                                                                Map.of("projectId", childProjectId))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "missing-required-input",
                                "spec", parentSpec));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertTrue(String.valueOf(payload.get("message")).contains("missing required"));
        }

        @Test
        void shouldAllowParameterizedSubWorkflowWhenRequiredInputProvided() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Parameterized Child OK",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "parameterized"),
                                                                "contract", Map.of(
                                                                                "inputs", Map.of(
                                                                                                "required",
                                                                                                List.of(Map.of("name",
                                                                                                                "ticketId",
                                                                                                                "type",
                                                                                                                "string")),
                                                                                                "optional",
                                                                                                List.of(Map.of("name",
                                                                                                                "priority",
                                                                                                                "type",
                                                                                                                "number"))))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-parameterized",
                                                                                                                "configuration",
                                                                                                                Map.of()),
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "agent"),
                                                                                                                "type",
                                                                                                                "agent-basic",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections",
                                                                                List.of(Map.of("fromNodeId", "start",
                                                                                                "toNodeId",
                                                                                                "agent")))))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> childProject = (Map<String, Object>) childCreated.getEntity();
                String childProjectId = String.valueOf(childProject.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parentProject = (Map<String, Object>) parentCreated.getEntity();
                String parentProjectId = String.valueOf(parentProject.get("projectId"));

                Map<String, Object> parentSpec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata",
                                                                                Map.of("id", "custom-child"),
                                                                                "type", "sub-workflow",
                                                                                "configuration", Map.of(
                                                                                                "projectId",
                                                                                                childProjectId,
                                                                                                "inputs", Map.of(
                                                                                                                "ticketId",
                                                                                                                "INC-123",
                                                                                                                "priority",
                                                                                                                5)))),
                                                "connections", List.of()));

                Response execute = resource.createExecution(parentProjectId, "community", Map.of(
                                "name", "with-required-input",
                                "spec", parentSpec));
                assertEquals(202, execute.getStatus());
        }

        @Test
        void shouldPreviewOutputBindingsForCallableProject() {
                Response childCreated = resource.createProject(Map.of(
                                "projectName", "Output Contract Child",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "manual"),
                                                                "contract", Map.of(
                                                                                "output", Map.of(
                                                                                                "type", "object",
                                                                                                "properties", Map.of(
                                                                                                                "summary",
                                                                                                                Map.of("type", "string"),
                                                                                                                "score",
                                                                                                                Map.of("type", "number"))))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-manual",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) childCreated.getEntity();
                String childId = String.valueOf(child.get("projectId"));

                Response preview = resource.previewOutputBindings(
                                childId,
                                "community",
                                "wayang_kulit",
                                Map.of(
                                                "configuration", Map.of(
                                                                "projectId", childId,
                                                                "outputBindings", Map.of(
                                                                                "summary", "context.child.summary",
                                                                                "unknown", "context.child.unknown"))));
                assertEquals(200, preview.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) preview.getEntity();
                assertEquals(Boolean.FALSE, payload.get("valid"));
                @SuppressWarnings("unchecked")
                List<String> invalid = (List<String>) payload.get("invalidSources");
                assertTrue(invalid.contains("unknown"));
        }

        @Test
        void shouldReturnStructuredInvalidWorkflowErrorPayload() {
                fakeDefinitionService.throwOnCreateMessage = "Invalid workflow definition";
                Response created = resource.createProject(Map.of("projectName", "Invalid Workflow Project"));
                assertEquals(201, created.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> project = (Map<String, Object>) created.getEntity();
                String projectId = String.valueOf(project.get("projectId"));

                Response execute = resource.createExecution(projectId, "community", Map.of(
                                "name", "invalid-workflow",
                                "spec", Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
                assertEquals(400, execute.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) execute.getEntity();
                assertEquals("EXECUTION_WORKFLOW_INVALID", String.valueOf(payload.get("errorCode")));
        }

        @Test
        void shouldExposeCallableContractEndpoint() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Contract Child",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "parameterized"),
                                                                "version", "v1",
                                                                "contract", Map.of(
                                                                                "inputs", Map.of(
                                                                                                "required",
                                                                                                List.of(Map.of("name",
                                                                                                                "ticketId",
                                                                                                                "type",
                                                                                                                "string"))),
                                                                                "output", Map.of(
                                                                                                "type", "object",
                                                                                                "properties",
                                                                                                Map.of("summary", Map
                                                                                                                .of("type", "string"))))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-parameterized",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) childCreated.getEntity();
                String childId = String.valueOf(child.get("projectId"));

                Response contract = resource.getCallableContract(childId, "community", "owner");
                assertEquals(200, contract.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) contract.getEntity();
                @SuppressWarnings("unchecked")
                Map<String, Object> callable = (Map<String, Object>) payload.get("callable");
                assertEquals("callable", String.valueOf(callable.get("mode")));
                assertEquals("v1", String.valueOf(callable.get("version")));
        }

        @Test
        void shouldValidateCallableContractEndpointWithMissingInput() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Validate Child",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "parameterized"),
                                                                "contract", Map.of(
                                                                                "inputs", Map.of(
                                                                                                "required",
                                                                                                List.of(Map.of("name",
                                                                                                                "ticketId",
                                                                                                                "type",
                                                                                                                "string"))))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-parameterized",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) childCreated.getEntity();
                String childId = String.valueOf(child.get("projectId"));

                Response invalid = resource.validateCallableContract(childId, "community", "owner", Map.of(
                                "nodeId", "custom-1",
                                "configuration", Map.of("projectId", childId)));
                assertEquals(400, invalid.getStatus());
        }

        @Test
        void shouldRejectSubWorkflowWhenPinnedVersionMismatch() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Versioned Child",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "version", "v2",
                                                                "entrypoint", Map.of("type", "manual")),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-manual",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) childCreated.getEntity();
                String childId = String.valueOf(child.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parent = (Map<String, Object>) parentCreated.getEntity();
                String parentId = String.valueOf(parent.get("projectId"));

                Map<String, Object> spec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "child"),
                                                                                "type", "sub-workflow",
                                                                                "configuration", Map.of(
                                                                                                "projectId", childId,
                                                                                                "projectVersion",
                                                                                                "v1"))),
                                                "connections", List.of()));
                Response execute = resource.createExecution(parentId, "community",
                                Map.of("name", "version-mismatch", "spec", spec));
                assertEquals(400, execute.getStatus());
        }

        @Test
        void shouldRejectSubWorkflowWhenOutputBindingRefersUnknownField() {
                Response childCreated = resource.createProject(Map.of(
                                "tenantId", "community",
                                "createdBy", "owner",
                                "projectName", "Output Child",
                                "metadata", Map.of(
                                                "reuse", Map.of(
                                                                "enabled", true,
                                                                "mode", "callable",
                                                                "entrypoint", Map.of("type", "manual"),
                                                                "contract", Map.of(
                                                                                "output", Map.of(
                                                                                                "type", "object",
                                                                                                "properties",
                                                                                                Map.of("summary", Map
                                                                                                                .of("type", "string"))))),
                                                "wayangSpec", Map.of(
                                                                "specVersion", "1.0.0",
                                                                "workflow", Map.of(
                                                                                "nodes", List.of(
                                                                                                Map.of("metadata", Map
                                                                                                                .of("id", "start"),
                                                                                                                "type",
                                                                                                                "trigger-manual",
                                                                                                                "configuration",
                                                                                                                Map.of())),
                                                                                "connections", List.of())))));
                assertEquals(201, childCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> child = (Map<String, Object>) childCreated.getEntity();
                String childId = String.valueOf(child.get("projectId"));

                Response parentCreated = resource.createProject(Map.of("projectName", "Parent"));
                assertEquals(201, parentCreated.getStatus());
                @SuppressWarnings("unchecked")
                Map<String, Object> parent = (Map<String, Object>) parentCreated.getEntity();
                String parentId = String.valueOf(parent.get("projectId"));

                Map<String, Object> spec = Map.of(
                                "specVersion", "1.0.0",
                                "workflow", Map.of(
                                                "nodes", List.of(
                                                                Map.of(
                                                                                "metadata", Map.of("id", "child"),
                                                                                "type", "sub-workflow",
                                                                                "configuration", Map.of(
                                                                                                "projectId", childId,
                                                                                                "outputBindings",
                                                                                                Map.of("unknownField",
                                                                                                                "parent.result")))),
                                                "connections", List.of()));
                Response execute = resource.createExecution(parentId, "community",
                                Map.of("name", "output-binding-invalid", "spec", spec));
                assertEquals(400, execute.getStatus());
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
                assertEquals(1, fakeDefinitionService.publishCalls.get(),
                                "duplicate idempotent submit must not publish");
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

                assertTrue(!firstExecutionId.equals(secondExecutionId),
                                "replay disabled should create a new execution");
                assertEquals(2, fakeDefinitionService.createCalls.get(), "disabled replay should allow second create");
                assertEquals(2, fakeDefinitionService.publishCalls.get(),
                                "disabled replay should allow second publish");
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
                                        "spec",
                                        Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
                        assertEquals(202, first.getStatus());
                        assertNotNull(first.getHeaderString("X-RateLimit-Limit"));
                        assertNotNull(first.getHeaderString("X-RateLimit-Remaining"));
                        assertNotNull(first.getHeaderString("X-RateLimit-Reset"));

                        Response second = resource.createExecution(projectId, tenantId, Map.of(
                                        "name", "rate-limit-run-2",
                                        "spec",
                                        Map.of("specVersion", "1.0.0", "workflow", Map.of("nodes", List.of()))));
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
                                System.setProperty("wayang.runtime.standalone.execution.rate-limit.enabled",
                                                originalEnabled);
                        }
                        if (originalLimit == null) {
                                System.clearProperty("wayang.runtime.standalone.execution.rate-limit.per-minute");
                        } else {
                                System.setProperty("wayang.runtime.standalone.execution.rate-limit.per-minute",
                                                originalLimit);
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
                public ValidationResult validateComprehensive(String schema, ValidationRule[] rules,
                                Map<String, Object> data) {
                        return ValidationResult.success();
                }
        }

        private static final class FakeDefinitionService extends WayangDefinitionService {
                private final AtomicInteger createCalls = new AtomicInteger();
                private final AtomicInteger publishCalls = new AtomicInteger();
                private final AtomicInteger runCalls = new AtomicInteger();
                private WayangSpec lastCreatedSpec;
                private String throwOnCreateMessage;

                @Override
                public Uni<WayangDefinition> create(String tenantId, UUID projectId, String name,
                                String description, DefinitionType type, WayangSpec spec, String createdBy) {
                        if (throwOnCreateMessage != null) {
                                return Uni.createFrom().failure(new RuntimeException(throwOnCreateMessage));
                        }
                        createCalls.incrementAndGet();
                        WayangDefinition definition = new WayangDefinition();
                        definition.definitionId = UUID.randomUUID();
                        definition.tenantId = tenantId;
                        definition.projectId = projectId;
                        definition.name = name;
                        definition.description = description;
                        definition.definitionType = type;
                        definition.spec = spec;
                        lastCreatedSpec = spec;
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
