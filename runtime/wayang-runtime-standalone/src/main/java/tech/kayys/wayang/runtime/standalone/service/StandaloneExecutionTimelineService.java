package tech.kayys.wayang.runtime.standalone.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.event.ExecutionEvent;
import tech.kayys.gamelan.engine.event.NodeCompletedEvent;
import tech.kayys.gamelan.engine.event.NodeFailedEvent;
import tech.kayys.gamelan.engine.event.NodeScheduledEvent;
import tech.kayys.gamelan.engine.event.NodeStartedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class StandaloneExecutionTimelineService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP = new TypeReference<>() {
    };

    public Uni<Void> recordEngineEvents(List<ExecutionEvent> events) {
        if (events == null || events.isEmpty()) {
            return Uni.createFrom().voidItem();
        }
        return Uni.createFrom().item(() -> {
            try {
                persistEngineEvents(events);
            } catch (Exception ignored) {
                // Best-effort timeline enrichment. Never block runtime execution path.
            }
            return null;
        }).replaceWithVoid();
    }

    private synchronized void persistEngineEvents(List<ExecutionEvent> events) throws IOException {
        final List<Map<String, Object>> executions = readJsonList(executionsFile());
        final Map<String, String> runToProject = executions.stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getOrDefault("executionId", "")),
                        e -> String.valueOf(e.getOrDefault("projectId", "unknown")),
                        (a, b) -> a));

        final List<Map<String, Object>> timeline = readJsonList(eventsFile());
        for (ExecutionEvent event : events) {
            if (event == null || event.runId() == null) {
                continue;
            }
            final String runId = event.runId().value();
            if (runId == null || runId.isBlank()) {
                continue;
            }

            final String projectId = runToProject.getOrDefault(runId, "unknown");
            final Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventId", event.eventId() != null ? event.eventId() : UUID.randomUUID().toString());
            payload.put("projectId", projectId);
            payload.put("executionId", runId);
            payload.put("type", event.eventType());
            payload.put("status", mapStatus(event.eventType()));
            payload.put("message", mapMessage(event));
            payload.put("createdAt", event.occurredAt() != null ? event.occurredAt().toString() : Instant.now().toString());
            payload.put("source", "gamelan-engine");

            final String nodeId = extractNodeId(event);
            if (nodeId != null && !nodeId.isBlank()) {
                payload.put("nodeId", nodeId);
            }

            final Map<String, Object> metadata = new LinkedHashMap<>();
            final Integer attempt = extractAttempt(event);
            if (attempt != null) {
                metadata.put("attempt", attempt);
            }
            if (event instanceof NodeFailedEvent failed && failed.error() != null) {
                metadata.put("error", String.valueOf(failed.error().message()));
                metadata.put("willRetry", failed.willRetry());
            }
            if (event instanceof NodeCompletedEvent completed && completed.output() != null) {
                metadata.put("output", completed.output());
                Map<String, Object> telemetry = extractTelemetry(completed.output());
                if (!telemetry.isEmpty()) {
                    metadata.put("telemetry", telemetry);
                }
            }
            payload.put("metadata", metadata);

            timeline.add(payload);
        }

        writeJsonList(eventsFile(), timeline);
    }

    private String mapStatus(String eventType) {
        if (eventType == null) return "UNKNOWN";
        final String normalized = eventType.toUpperCase();
        if (normalized.contains("NODE_STARTED")) return "RUNNING";
        if (normalized.contains("NODE_SCHEDULED")) return "SCHEDULED";
        if (normalized.contains("NODE_COMPLETED")) return "COMPLETED";
        if (normalized.contains("NODE_FAILED")) return "FAILED";
        if (normalized.contains("WORKFLOW_COMPLETED")) return "COMPLETED";
        if (normalized.contains("WORKFLOW_FAILED")) return "FAILED";
        if (normalized.contains("WORKFLOW_SUSPENDED")) return "PAUSED";
        if (normalized.contains("WORKFLOW_RESUMED")) return "RUNNING";
        if (normalized.contains("WORKFLOW_CANCELLED")) return "CANCELLED";
        return normalized;
    }

    private String mapMessage(ExecutionEvent event) {
        if (event instanceof NodeFailedEvent failed && failed.error() != null) {
            return "Node failed: " + failed.error().message();
        }
        final String eventType = event.eventType() != null ? event.eventType() : "EVENT";
        return eventType.replace('_', ' ');
    }

    private String extractNodeId(ExecutionEvent event) {
        if (event instanceof NodeStartedEvent started && started.nodeId() != null) {
            return started.nodeId().value();
        }
        if (event instanceof NodeScheduledEvent scheduled && scheduled.nodeId() != null) {
            return scheduled.nodeId().value();
        }
        if (event instanceof NodeCompletedEvent completed && completed.nodeId() != null) {
            return completed.nodeId().value();
        }
        if (event instanceof NodeFailedEvent failed && failed.nodeId() != null) {
            return failed.nodeId().value();
        }
        return null;
    }

    private Integer extractAttempt(ExecutionEvent event) {
        if (event instanceof NodeStartedEvent started) {
            return started.attempt();
        }
        if (event instanceof NodeScheduledEvent scheduled) {
            return scheduled.attempt();
        }
        if (event instanceof NodeCompletedEvent completed) {
            return completed.attempt();
        }
        if (event instanceof NodeFailedEvent failed) {
            return failed.attempt();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractTelemetry(Map<String, Object> output) {
        if (output == null || output.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> telemetry = new LinkedHashMap<>();

        Object orchestrationType = output.get("orchestrationType");
        if (orchestrationType != null) {
            telemetry.put("orchestrationType", orchestrationType);
        }
        Object tasksExecuted = output.get("tasksExecuted");
        if (tasksExecuted != null) {
            telemetry.put("tasksExecuted", tasksExecuted);
        }
        Object executionMode = output.get("executionMode");
        if (executionMode != null) {
            telemetry.put("executionMode", executionMode);
        }
        Object executionOrder = output.get("executionOrder");
        if (executionOrder != null) {
            telemetry.put("executionOrder", executionOrder);
        }

        Object budget = output.get("budget");
        if (budget instanceof Map<?, ?> budgetMap && !budgetMap.isEmpty()) {
            telemetry.put("budget", new LinkedHashMap<>((Map<String, Object>) budgetMap));
        }
        Object outputTelemetry = output.get("telemetry");
        if (outputTelemetry instanceof Map<?, ?> telemetryMap && !telemetryMap.isEmpty()) {
            telemetry.put("executorTelemetry", new LinkedHashMap<>((Map<String, Object>) telemetryMap));
        }

        return telemetry;
    }

    private Path executionsFile() throws IOException {
        return ensureServerLogDir().resolve("cloud-project-executions.json");
    }

    private Path eventsFile() throws IOException {
        return ensureServerLogDir().resolve("cloud-project-execution-events.json");
    }

    private Path ensureServerLogDir() throws IOException {
        final String userHome = System.getProperty("user.home", ".");
        final Path dir = Paths.get(userHome, ".wayang", "logs", "server");
        Files.createDirectories(dir);
        return dir;
    }

    private List<Map<String, Object>> readJsonList(Path file) throws IOException {
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

    private void writeJsonList(Path file, List<Map<String, Object>> payload) throws IOException {
        Files.writeString(file, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
    }
}
