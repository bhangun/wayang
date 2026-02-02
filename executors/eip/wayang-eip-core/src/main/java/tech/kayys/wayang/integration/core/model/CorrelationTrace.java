package tech.kayys.wayang.integration.core.model;

import java.time.Instant;
import java.util.List;

public record CorrelationTrace(
                String correlationId,
                List<TracePoint> tracePoints,
                Instant startedAt,
                Instant expiresAt) {
}
