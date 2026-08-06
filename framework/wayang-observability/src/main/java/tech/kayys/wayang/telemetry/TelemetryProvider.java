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


import java.time.Instant;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Extension;

/**
 * Telemetry Provider - provides telemetry.
 */
public interface TelemetryProvider extends Extension {
    
    /**
     * Record a trace
     */
    void trace(TraceEvent event) throws Exception;
    
    /**
     * Record a metric
     */
    void metric(Metric metric) throws Exception;
    
    /**
     * Record a log
     */
    void log(LogEvent event) throws Exception;
    
    /**
     * Start a trace
     */
    default TraceContext startTrace(String name) throws Exception {
        return new TraceContext(Id.random().asString(), name, Instant.now());
    }
    
    /**
     * End a trace
     */
    default void endTrace(TraceContext context) throws Exception {
        trace(new TraceEvent(context.id(), context.name(), context.startTime(), 
            Instant.now(), Map.of(), "OK"));
    }
    
    /**
     * Flush telemetry
     */
    default void flush() throws Exception {
        // Optional
    }
}
