package tech.kayys.wayang.a2a.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a durable task in the A2A protocol.
 */
public record A2ATask(
    String taskId,
    String executionId,
    A2ATaskStatus status,
    List<A2AArtifact> artifacts,
    Map<String, Object> metadata
) {}
