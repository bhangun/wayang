package tech.kayys.wayang.eip.model;

import java.time.Instant;
import java.util.List;

public record CorrelationTrace(
        String correlationId,
        List<TracePoint> tracePoints,
        Instant startedAt,
        Instant expiresAt) {
}
