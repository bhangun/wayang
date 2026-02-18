package tech.kayys.wayang.eip.service;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.eip.model.CorrelationTrace;
import tech.kayys.wayang.eip.model.TracePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CorrelationService {

    private static final Logger LOG = LoggerFactory.getLogger(CorrelationService.class);

    private final ConcurrentHashMap<String, CorrelationTrace> traces = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    void init() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.HOURS);
    }

    public void track(String correlationId, String runId, String nodeId) {
        traces.compute(correlationId, (key, existing) -> {
            TracePoint point = new TracePoint(runId, nodeId, Instant.now());

            if (existing == null) {
                List<TracePoint> points = Collections.synchronizedList(new ArrayList<>());
                points.add(point);
                return new CorrelationTrace(
                        correlationId,
                        points,
                        Instant.now(),
                        Instant.now().plus(Duration.ofDays(1)));
            } else {
                existing.tracePoints().add(point);
                return existing;
            }
        });

        LOG.debug("Tracked correlation: {} at node: {}", correlationId, nodeId);
    }

    public Uni<CorrelationTrace> getTrace(String correlationId) {
        return Uni.createFrom().item(() -> traces.get(correlationId));
    }

    public Uni<Map<String, Object>> getTraceVisualization(String correlationId) {
        return Uni.createFrom().item(() -> {
            CorrelationTrace trace = traces.get(correlationId);
            if (trace == null) {
                return Map.of("found", false);
            }

            List<Map<String, Object>> points = trace.tracePoints().stream()
                    .map(point -> {
                        Map<String, Object> p = new HashMap<>();
                        p.put("runId", point.runId());
                        p.put("nodeId", point.nodeId());
                        p.put("timestamp", point.timestamp().toString());
                        return p;
                    })
                    .toList();

            return Map.of(
                    "correlationId", correlationId,
                    "startedAt", trace.startedAt().toString(),
                    "tracePoints", points,
                    "nodeCount", points.size());
        });
    }

    private void cleanup() {
        try {
            Instant now = Instant.now();
            List<String> expired = new ArrayList<>();

            traces.forEach((id, trace) -> {
                if (now.isAfter(trace.expiresAt())) {
                    expired.add(id);
                }
            });

            expired.forEach(traces::remove);

            if (!expired.isEmpty()) {
                LOG.info("Cleaned up {} expired correlation traces", expired.size());
            }
        } catch (Exception e) {
            LOG.error("Correlation cleanup failed", e);
        }
    }

    @PreDestroy
    void shutdown() {
        cleanupScheduler.shutdown();
    }
}
