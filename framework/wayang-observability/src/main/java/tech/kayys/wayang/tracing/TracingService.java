package tech.kayys.wayang.tracing;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import tech.kayys.wayang.extension.Extension;

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

    // Lifecycle
    void flush() throws Exception;
    void shutdown() throws Exception;
}
