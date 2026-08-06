package tech.kayys.wayang.telemetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default Metrics Implementation
 */
public class DefaultMetrics implements Metrics {
    
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();
    private final Map<String, HistogramData> histograms = new ConcurrentHashMap<>();
    private final Map<String, TimerData> timers = new ConcurrentHashMap<>();
    private final String namespace;
    
    public DefaultMetrics(String namespace) {
        this.namespace = namespace;
    }
    
    @Override
    public void counter(String name, long increment) {
        counters.computeIfAbsent(namespace + "." + name, k -> new AtomicLong())
            .addAndGet(increment);
    }
    
    @Override
    public void gauge(String name, double value) {
        gauges.computeIfAbsent(namespace + "." + name, k -> new AtomicLong())
            .set(Double.doubleToRawLongBits(value));
    }
    
    @Override
    public void histogram(String name, double value) {
        histograms.computeIfAbsent(namespace + "." + name, k -> new HistogramData())
            .record(value);
    }
    
    @Override
    public void timer(String name, long durationMs) {
        timers.computeIfAbsent(namespace + "." + name, k -> new TimerData())
            .record(durationMs);
    }
    
    @Override
    public Map<String, MetricValue> snapshot() {
        Map<String, MetricValue> result = new HashMap<>();
        
        for (Map.Entry<String, AtomicLong> entry : counters.entrySet()) {
            result.put(entry.getKey(), new MetricValue(
                entry.getKey(),
                MetricType.COUNTER,
                entry.getValue().doubleValue(),
                0, 0, 0, 0, 0, 0, 0,
                Map.of()
            ));
        }
        
        // Similar for gauges, histograms, timers...
        
        return result;
    }
    
    @Override
    public void reset() {
        counters.clear();
        gauges.clear();
        histograms.clear();
        timers.clear();
    }
    
    private static class HistogramData {
        private final List<Double> values = new ArrayList<>();
        
        synchronized void record(double value) {
            values.add(value);
            if (values.size() > 1000) {
                values.remove(0);
            }
        }
    }
    
    private static class TimerData {
        private final List<Long> values = new ArrayList<>();
        
        synchronized void record(long value) {
            values.add(value);
            if (values.size() > 1000) {
                values.remove(0);
            }
        }
    }
}