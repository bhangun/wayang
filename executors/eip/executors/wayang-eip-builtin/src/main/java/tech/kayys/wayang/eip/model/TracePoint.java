package tech.kayys.wayang.eip.model;

import java.time.Instant;

public record TracePoint(String runId, String nodeId, Instant timestamp) {
}
