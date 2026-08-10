package tech.kayys.wayang.tracing;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import tech.kayys.wayang.configuration.ConfigurationResource;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * OpenTelemetry Implementation
 */
public class OpenTelemetryTracingService implements TracingService {

    private final ResourceId resourceId;
    private final Metadata metadata;
    private final Tracer tracer;
    private final Map<String, Span> spanStack = new ConcurrentHashMap<>();
    private final ThreadLocal<Context> currentContext = new ThreadLocal<>();
    private volatile boolean initialized = false;

    public OpenTelemetryTracingService(ConfigurationResource config) {
        ResourceType tracingType = new ResourceType.Custom("tracing");
        this.resourceId = new ResourceId.CustomId(Id.random(), tracingType);
        this.metadata = Metadata.builder()
            .name("tracing-service")
            .description("OpenTelemetry Tracing Service")
            .version("1.0.0")
            .label("type", "tracing")
            .label("provider", "opentelemetry")
            .now()
            .build();
        // Use GlobalOpenTelemetry — SDK is configured externally (e.g., via Quarkus OTel extension)
        this.tracer = GlobalOpenTelemetry.getTracer("wayang", "1.0.0");
    }

    @Override
    public ResourceId id() { return resourceId; }

    @Override
    public Metadata metadata() { return metadata; }

    @Override
    public ResourceType type() { return resourceId.type(); }

    @Override
    public Span startSpan(String name) {
        return startSpan(name, SpanKind.INTERNAL);
    }

    @Override
    public Span startSpan(String name, SpanKind kind) {
        Span span = tracer.spanBuilder(name)
            .setSpanKind(kind)
            .setParent(Context.current())
            .startSpan();
        spanStack.put(span.getSpanContext().getSpanId(), span);
        return span;
    }

    @Override
    public Span startSpan(String name, Map<String, Object> attributes) {
        Span span = startSpan(name);
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            span.setAttribute(entry.getKey(), entry.getValue().toString());
        }
        return span;
    }

    @Override
    public Span startSpan(String name, Context parent) {
        Span span = tracer.spanBuilder(name)
            .setParent(parent)
            .startSpan();
        spanStack.put(span.getSpanContext().getSpanId(), span);
        return span;
    }

    @Override
    public Optional<Span> currentSpan() {
        return Optional.ofNullable(Span.current());
    }

    @Override
    public Span getCurrentSpan() {
        return Span.current();
    }

    @Override
    public Context getCurrentContext() {
        Context ctx = currentContext.get();
        return ctx != null ? ctx : Context.current();
    }

    @Override
    public void setCurrentContext(Context context) {
        currentContext.set(context);
    }

    @Override
    public <T> T withSpan(String name, Callable<T> callable) throws Exception {
        Span span = startSpan(name);
        try (Scope scope = span.makeCurrent()) {
            return callable.call();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void withSpan(String name, Runnable runnable) {
        Span span = startSpan(name);
        try (Scope scope = span.makeCurrent()) {
            runnable.run();
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void addAttribute(String key, Object value) {
        Span span = getCurrentSpan();
        if (span != null) {
            span.setAttribute(key, value.toString());
        }
    }

    @Override
    public void addEvent(String name) {
        Span span = getCurrentSpan();
        if (span != null) {
            span.addEvent(name);
        }
    }

    @Override
    public void addEvent(String name, Map<String, Object> attributes) {
        Span span = getCurrentSpan();
        if (span != null) {
            span.addEvent(name);
        }
    }

    @Override
    public void recordException(Throwable error) {
        Span span = getCurrentSpan();
        if (span != null) {
            span.recordException(error);
            span.setStatus(StatusCode.ERROR);
        }
    }

    @Override
    public void setStatus(StatusCode status) {
        Span span = getCurrentSpan();
        if (span != null) {
            span.setStatus(status);
        }
    }

    @Override
    public void flush() throws Exception {
        // No-op: SDK is managed externally
    }

    @Override
    public void shutdown() throws Exception {
        // No-op: SDK is managed externally
    }

    @Override
    public void initialize() throws Exception {
        initialized = true;
    }
}