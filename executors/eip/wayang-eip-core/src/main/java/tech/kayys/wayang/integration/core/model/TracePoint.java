package tech.kayys.wayang.integration.core.model;

import java.time.Instant;

public record TracePoint(String runId, String nodeId, Instant timestamp) {
}
