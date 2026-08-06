package tech.kayys.wayang.tracing;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import io.opentelemetry.api.*;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.*;
import io.opentelemetry.sdk.*;
import io.opentelemetry.sdk.trace.*;
import io.opentelemetry.sdk.trace.export.*;
import io.opentelemetry.exporter.otlp.trace.*;
import io.opentelemetry.sdk.resources.*;
import io.opentelemetry.semconv.*;
import java.util.*;
import java.time.*;

/**
 * Tracing Service - OpenTelemetry Integration
 */
public interface TracingService extends Extension {
    
    // Span creation
    Span startSpan(String name);
    Span startSpan(String name, SpanKind kind);
    Span startSpan(String name, Map<String, Object> attributes);
    Span startSpan(String name, Context parent);
    
    // Current span
    Optional<Span> currentSpan();
    Span getCurrentSpan();
    
    // Context propagation
    Context getCurrentContext();
    void setCurrentContext(Context context);
    
    // Trace propagation
    <T> T withSpan(String name, Callable<T> callable) throws Exception;
    void withSpan(String name, Runnable runnable);
    
    // Attributes
    void addAttribute(String key, Object value);
    void addEvent(String name);
    void addEvent(String name, Map<String, Object> attributes);
    
    // Error handling
    void recordException(Throwable error);
    void setStatus(StatusCode status);
    
    // Export
    void flush() throws Exception;
    void shutdown() throws Exception;
}
