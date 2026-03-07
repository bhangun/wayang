package tech.kayys.wayang.runtime.standalone.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.kayys.gamelan.engine.event.ExecutionEvent;
import tech.kayys.gamelan.engine.event.NodeCompletedEvent;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneExecutionTimelineServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP = new TypeReference<>() {
    };

    @TempDir
    Path tempDir;

    @Test
    void recordEngineEventsAddsTelemetryForNodeCompletedOutput() throws Exception {
        String previousUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        try {
            Path serverLogDir = tempDir.resolve(".wayang").resolve("logs").resolve("server");
            Files.createDirectories(serverLogDir);

            String runIdValue = "run-telemetry-1";
            List<Map<String, Object>> executions = List.of(Map.of(
                    "executionId", runIdValue,
                    "projectId", "project-1"));
            Files.writeString(
                    serverLogDir.resolve("cloud-project-executions.json"),
                    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(executions));

            Map<String, Object> output = Map.of(
                    "orchestrationType", "SEQUENTIAL",
                    "tasksExecuted", 2,
                    "executionOrder", "sequential",
                    "telemetry", Map.of(
                            "delegationAttempts", 3,
                            "delegationRetries", 1,
                            "delegationFailures", 1,
                            "delegationTimeouts", 0),
                    "budget", Map.of(
                            "maxIterations", 2,
                            "maxDelegations", 3,
                            "maxRetriesPerDelegation", 1));
            ExecutionEvent completed = new NodeCompletedEvent(
                    "evt-1",
                    new WorkflowRunId(runIdValue),
                    new NodeId("orchestrator-node"),
                    1,
                    output,
                    Instant.now());

            StandaloneExecutionTimelineService service = new StandaloneExecutionTimelineService();
            service.recordEngineEvents(List.of(completed)).await().indefinitely();

            List<Map<String, Object>> events = OBJECT_MAPPER.readValue(
                    Files.readString(serverLogDir.resolve("cloud-project-execution-events.json")),
                    LIST_OF_MAP);
            assertEquals(1, events.size());

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) events.get(0).get("metadata");
            assertNotNull(metadata);
            @SuppressWarnings("unchecked")
            Map<String, Object> telemetry = (Map<String, Object>) metadata.get("telemetry");
            assertNotNull(telemetry);
            assertEquals("SEQUENTIAL", telemetry.get("orchestrationType"));
            assertEquals(2, telemetry.get("tasksExecuted"));
            assertEquals("sequential", telemetry.get("executionOrder"));
            @SuppressWarnings("unchecked")
            Map<String, Object> budget = (Map<String, Object>) telemetry.get("budget");
            assertNotNull(budget);
            assertEquals(2, budget.get("maxIterations"));
            assertEquals(3, budget.get("maxDelegations"));
            assertEquals(1, budget.get("maxRetriesPerDelegation"));
            @SuppressWarnings("unchecked")
            Map<String, Object> executorTelemetry = (Map<String, Object>) telemetry.get("executorTelemetry");
            assertNotNull(executorTelemetry);
            assertEquals(3, executorTelemetry.get("delegationAttempts"));
            assertEquals(1, executorTelemetry.get("delegationRetries"));
            assertTrue(metadata.containsKey("output"));
        } finally {
            if (previousUserHome != null) {
                System.setProperty("user.home", previousUserHome);
            }
        }
    }
}
