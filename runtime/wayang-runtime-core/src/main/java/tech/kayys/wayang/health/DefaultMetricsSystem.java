package tech.kayys.wayang.health;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import tech.kayys.wayang.telemetry.MetricValue;
import tech.kayys.wayang.telemetry.Metrics;

import java.time.*;

/**
 * Complete Metrics System
 */
public class DefaultMetricsSystem implements MetricsSystem {
    
    private final Map<String, Metrics> metricsMap = new ConcurrentHashMap<>();
    private final Map<String, MetricRegistry> registries = new ConcurrentHashMap<>();
    private final List<MetricsExporter> exporters = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public DefaultMetricsSystem() {
        // Schedule periodic export
        scheduler.scheduleAtFixedRate(() -> {
            try {
                export();
            } catch (Exception e) {
                // Ignore
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    @Override
    public Metrics getMetrics(String namespace) {
        return metricsMap.computeIfAbsent(namespace, DefaultMetrics::new);
    }
    
    @Override
    public void registerExporter(MetricsExporter exporter) {
        exporters.add(exporter);
    }
    
    @Override
    public void unregisterExporter(MetricsExporter exporter) {
        exporters.remove(exporter);
    }
    
    @Override
    public void export() throws Exception {
        for (MetricsExporter exporter : exporters) {
            Map<String, MetricValue> snapshot = snapshot();
            exporter.export(snapshot);
        }
    }
    
    @Override
    public Map<String, MetricValue> snapshot() {
        Map<String, MetricValue> result = new LinkedHashMap<>();
        for (Metrics metrics : metricsMap.values()) {
            result.putAll(metrics.snapshot());
        }
        return result;
    }
    
    @Override
    public void reset() {
        for (Metrics metrics : metricsMap.values()) {
            metrics.reset();
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
