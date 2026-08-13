package tech.kayys.wayang.execution.event;

import java.time.Instant;

/**
 * A durable record tracking the execution of a tool.
 */
public record ToolExecution(
    String executionId,
    String tenantId,
    String userId,
    String toolId,
    String normalizedInput,
    long durationMs,
    boolean cacheHit,
    Instant timestamp
) {}
