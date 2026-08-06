package tech.kayys.wayang.telemetry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;

/**
 * Metrics System
 */
public interface Metrics {
    
    void counter(String name, long increment);
    
    void counter(String name, long increment, Map<String, String> tags);
    
    void gauge(String name, double value);
    
    void gauge(String name, double value, Map<String, String> tags);
    
    void histogram(String name, double value);
    
    void histogram(String name, double value, Map<String, String> tags);
    
    void timer(String name, long durationMs);
    
    void timer(String name, long durationMs, Map<String, String> tags);
    
    Map<String, MetricValue> snapshot();
    
    void reset();
}

