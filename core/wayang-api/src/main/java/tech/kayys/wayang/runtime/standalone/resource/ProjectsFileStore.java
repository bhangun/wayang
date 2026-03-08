package tech.kayys.wayang.runtime.standalone.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

final class ProjectsFileStore {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAP = new TypeReference<>() {};

    private ProjectsFileStore() {
    }

    static synchronized List<Map<String, Object>> readProjects() throws IOException {
        return readList(projectsFile());
    }

    static synchronized void writeProjects(List<Map<String, Object>> projects) throws IOException {
        writeList(projectsFile(), projects);
    }

    static synchronized List<Map<String, Object>> readExecutions() throws IOException {
        return readList(executionsFile());
    }

    static synchronized void writeExecutions(List<Map<String, Object>> executions) throws IOException {
        writeList(executionsFile(), executions);
    }

    static synchronized List<Map<String, Object>> readExecutionEvents() throws IOException {
        return readList(executionEventsFile());
    }

    static synchronized void writeExecutionEvents(List<Map<String, Object>> events) throws IOException {
        writeList(executionEventsFile(), events);
    }

    static synchronized void appendExecutionEvent(
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

    private static List<Map<String, Object>> readList(Path file) throws IOException {
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

    private static void writeList(Path file, List<Map<String, Object>> values) throws IOException {
        Files.writeString(file, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(values));
    }

    private static Path projectsFile() throws IOException {
        return storageDir().resolve("cloud-projects.json");
    }

    private static Path executionsFile() throws IOException {
        return storageDir().resolve("cloud-project-executions.json");
    }

    private static Path executionEventsFile() throws IOException {
        return storageDir().resolve("cloud-project-execution-events.json");
    }

    private static Path storageDir() throws IOException {
        final String userHome = System.getProperty("user.home", ".");
        final Path dir = Paths.get(userHome, ".wayang", "logs", "server");
        Files.createDirectories(dir);
        return dir;
    }
}
