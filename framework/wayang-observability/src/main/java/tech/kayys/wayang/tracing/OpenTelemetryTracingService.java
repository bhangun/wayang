package tech.kayys.wayang.tracing;

import java.lang.foreign.MemorySegment.Scope;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;

import tech.kayys.wayang.configuration.ConfigurationResource;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * OpenTelemetry Implementation
 */
public class OpenTelemetryTracingService implements TracingService {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;
    private final SpanProcessor spanProcessor;
    private final Map<String, Span> spanStack = new ConcurrentHashMap<>();
    private final ThreadLocal<Context> currentContext = new ThreadLocal<>();
    private volatile boolean initialized = false;
    
    public OpenTelemetryTracingService(ConfigurationResource config) {
        this.id = Id.random().asString();
        this.name = "tracing-service";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("OpenTelemetry Tracing Service")
            .version(version)
            .label("type", "tracing")
            .label("provider", "opentelemetry")
            .now()
            .build();
        
        // Build OpenTelemetry
        this.openTelemetry = buildOpenTelemetry(config);
        this.tracer = openTelemetry.getTracer("wayang", "1.0.0");
        this.spanProcessor = new SimpleSpanProcessor();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("tracing"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
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
        
        // Store in stack
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
        Context context = currentContext.get();
        return context != null ? context : Context.current();
    }
    
    @Override
    public void setCurrentContext(Context context) {
        currentContext.set(context);
    }
    
    @Override
    public <T> T withSpan(String name, Callable<T> callable) throws Exception {
        Span span = startSpan(name);
        try {
            try (Scope scope = span.makeCurrent()) {
                return callable.call();
            }
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
            span.addEvent(name, attributes);
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
        // Flush span processor
    }
    
    @Override
    public void shutdown() throws Exception {
        openTelemetry.close();
    }
    
    @Override
    public void initialize() throws Exception {
        if (!initialized) {
            // Register with OpenTelemetry
            OpenTelemetry.set(openTelemetry);
            initialized = true;
        }
    }
    
    private OpenTelemetry buildOpenTelemetry(ConfigurationResource config) {
        String endpoint = config.get("tracing.otlp.endpoint", String.class, "http://localhost:4317");
        
        // Create OTLP exporter
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(endpoint)
            .build();
        
        // Create span processor
        SpanProcessor processor = SimpleSpanProcessor.create(exporter);
        
        // Create tracer provider
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(processor)
            .setResource(Resource.create(
                Attributes.of(
                    ResourceAttributes.SERVICE_NAME, "wayang",
                    ResourceAttributes.SERVICE_VERSION, "1.0.0"
                )
            ))
            .build();
        
        // Create OpenTelemetry
        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();
    }
}