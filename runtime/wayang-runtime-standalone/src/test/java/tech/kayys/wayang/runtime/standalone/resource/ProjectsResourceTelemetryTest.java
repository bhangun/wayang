package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectsResourceTelemetryTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void getExecutionTelemetryAggregatesCountersFromTimelineEvents() throws Exception {
        String previousUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        try {
            Path serverLogDir = tempDir.resolve(".wayang").resolve("logs").resolve("server");
            Files.createDirectories(serverLogDir);

            List<Map<String, Object>> events = List.of(
                    Map.of(
                            "eventId", "evt-1",
                            "projectId", "project-1",
                            "executionId", "exec-1",
                            "type", "NODE_COMPLETED",
                            "nodeId", "node-a",
                            "createdAt", Instant.parse("2026-03-07T00:00:01Z").toString(),
                            "metadata", Map.of(
                                    "telemetry", Map.of(
                                            "orchestrationType", "SEQUENTIAL",
                                            "tasksExecuted", 2,
                                            "budget", Map.of("maxDelegations", 2),
                                            "executorTelemetry", Map.of(
                                                    "delegationAttempts", 3,
                                                    "delegationRetries", 1,
                                                    "delegationFailures", 1,
                                                    "delegationTimeouts", 0)))),
                    Map.of(
                            "eventId", "evt-2",
                            "projectId", "project-1",
                            "executionId", "exec-1",
                            "type", "NODE_FAILED",
                            "nodeId", "node-b",
                            "createdAt", Instant.parse("2026-03-07T00:00:02Z").toString(),
                            "metadata", Map.of(
                                    "telemetry", Map.of(
                                            "orchestrationType", "SEQUENTIAL",
                                            "tasksExecuted", 1,
                                            "budget", Map.of("maxDelegations", 3),
                                            "executorTelemetry", Map.of(
                                                    "delegationAttempts", 2,
                                                    "delegationRetries", 0,
                                                    "delegationFailures", 0,
                                                    "delegationTimeouts", 1)))));
            Files.writeString(
                    serverLogDir.resolve("cloud-project-execution-events.json"),
                    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(events));

            ProjectsService resource = new ProjectsService();
            Response response = resource.getExecutionTelemetry(
                    "project-1", "exec-1", null, null, null, null, null, null, null, false);
            assertEquals(200, response.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) response.getEntity();
            assertNotNull(payload);
            assertEquals("project-1", payload.get("projectId"));
            assertEquals("exec-1", payload.get("executionId"));
            assertEquals(2, payload.get("eventCount"));
            assertEquals(2, payload.get("telemetryEventCount"));
            @SuppressWarnings("unchecked")
            Map<String, Object> counters = (Map<String, Object>) payload.get("counters");
            assertEquals(3L, counters.get("tasksExecuted"));
            assertEquals(5L, counters.get("delegationAttempts"));
            assertEquals(1L, counters.get("delegationRetries"));
            assertEquals(1L, counters.get("delegationFailures"));
            assertEquals(1L, counters.get("delegationTimeouts"));
            @SuppressWarnings("unchecked")
            Map<String, Object> latestBudget = (Map<String, Object>) payload.get("latestBudget");
            assertEquals(3, latestBudget.get("maxDelegations"));

            Response filteredResponse = resource.getExecutionTelemetry(
                    "project-1",
                    "exec-1",
                    "2026-03-07T00:00:02Z",
                    "2026-03-07T00:00:03Z",
                    null,
                    null,
                    null,
                    null,
                    null,
                    false);
            assertEquals(200, filteredResponse.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> filteredPayload = (Map<String, Object>) filteredResponse.getEntity();
            assertEquals(1, filteredPayload.get("eventCount"));
            assertEquals(1, filteredPayload.get("telemetryEventCount"));
            @SuppressWarnings("unchecked")
            Map<String, Object> filteredCounters = (Map<String, Object>) filteredPayload.get("counters");
            assertEquals(1L, filteredCounters.get("tasksExecuted"));
            assertEquals(2L, filteredCounters.get("delegationAttempts"));
            @SuppressWarnings("unchecked")
            Map<String, Object> filters = (Map<String, Object>) filteredPayload.get("filters");
            assertEquals("2026-03-07T00:00:02Z", filters.get("from"));
            assertEquals("2026-03-07T00:00:03Z", filters.get("to"));

            Response groupedResponse = resource.getExecutionTelemetry(
                    "project-1",
                    "exec-1",
                    null,
                    null,
                    null,
                    null,
                    "nodeId",
                    null,
                    null,
                    false);
            assertEquals(200, groupedResponse.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> groupedPayload = (Map<String, Object>) groupedResponse.getEntity();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> grouped = (List<Map<String, Object>>) groupedPayload.get("grouped");
            assertEquals(2, grouped.size());
            assertEquals("node-a", grouped.get(0).get("nodeId"));
            assertEquals(2L, grouped.get(0).get("tasksExecuted"));
            assertEquals("node-b", grouped.get(1).get("nodeId"));
            assertEquals(1L, grouped.get(1).get("tasksExecuted"));

            Response groupedByTypeResponse = resource.getExecutionTelemetry(
                    "project-1",
                    "exec-1",
                    null,
                    null,
                    null,
                    null,
                    "type",
                    "tasksExecuted:desc",
                    1,
                    false);
            assertEquals(200, groupedByTypeResponse.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> groupedByTypePayload = (Map<String, Object>) groupedByTypeResponse.getEntity();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> groupedByType = (List<Map<String, Object>>) groupedByTypePayload.get("grouped");
            assertEquals(1, groupedByType.size());
            assertEquals("NODE_COMPLETED", groupedByType.get(0).get("type"));
            assertEquals(2L, groupedByType.get(0).get("tasksExecuted"));

            Response includeRawResponse = resource.getExecutionTelemetry(
                    "project-1",
                    "exec-1",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    true);
            assertEquals(200, includeRawResponse.getStatus());
            @SuppressWarnings("unchecked")
            Map<String, Object> includeRawPayload = (Map<String, Object>) includeRawResponse.getEntity();
            assertEquals(2, includeRawPayload.get("rawEventCount"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawEvents = (List<Map<String, Object>>) includeRawPayload.get("rawEvents");
            assertEquals(2, rawEvents.size());
        } finally {
            if (previousUserHome != null) {
                System.setProperty("user.home", previousUserHome);
            }
        }
    }
}
