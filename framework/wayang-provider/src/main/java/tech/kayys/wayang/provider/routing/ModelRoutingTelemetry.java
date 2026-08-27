package tech.kayys.wayang.provider.routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe telemetry tracker recording model performance, latency, errors, and token consumption
 * to adaptively influence routing scores at runtime.
 */
public class ModelRoutingTelemetry {

    private static final ModelRoutingTelemetry INSTANCE = new ModelRoutingTelemetry();

    public static ModelRoutingTelemetry getInstance() {
        return INSTANCE;
    }

    public static class ModelStats {
        public final AtomicLong totalCalls = new AtomicLong();
        public final AtomicLong successCalls = new AtomicLong();
        public final AtomicLong errorCalls = new AtomicLong();
        public final AtomicLong totalLatencyMs = new AtomicLong();
        public final AtomicLong totalTokens = new AtomicLong();

        public double errorRate() {
            long total = totalCalls.get();
            if (total == 0) return 0.0;
            return (double) errorCalls.get() / total;
        }

        public long averageLatencyMs() {
            long successes = successCalls.get();
            if (successes == 0) return 0;
            return totalLatencyMs.get() / successes;
        }

        public double computeHealthMultiplier() {
            long total = totalCalls.get();
            if (total < 3) return 1.0; // insufficient data

            double errRate = errorRate();
            if (errRate >= 0.50) return 0.1; // severely degraded
            if (errRate >= 0.20) return 0.5; // degraded
            if (errRate >= 0.05) return 0.85;

            return 1.0;
        }
    }

    private final Map<String, ModelStats> statsMap = new ConcurrentHashMap<>();

    public void recordSuccess(String modelId, long latencyMs, long tokens) {
        if (modelId == null) return;
        ModelStats stats = statsMap.computeIfAbsent(modelId.toLowerCase(), k -> new ModelStats());
        stats.totalCalls.incrementAndGet();
        stats.successCalls.incrementAndGet();
        stats.totalLatencyMs.addAndGet(latencyMs);
        stats.totalTokens.addAndGet(tokens);
    }

    public void recordFailure(String modelId, long latencyMs) {
        if (modelId == null) return;
        ModelStats stats = statsMap.computeIfAbsent(modelId.toLowerCase(), k -> new ModelStats());
        stats.totalCalls.incrementAndGet();
        stats.errorCalls.incrementAndGet();
        stats.totalLatencyMs.addAndGet(latencyMs);
    }

    public double getHealthMultiplier(String modelId) {
        if (modelId == null) return 1.0;
        ModelStats stats = statsMap.get(modelId.toLowerCase());
        if (stats == null) return 1.0;
        return stats.computeHealthMultiplier();
    }

    public ModelStats getStats(String modelId) {
        if (modelId == null) return null;
        return statsMap.get(modelId.toLowerCase());
    }

    public void reset() {
        statsMap.clear();
    }
}
