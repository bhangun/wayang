package tech.kayys.wayang.telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultTelemetryProvider implements TelemetryProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final List<TraceEvent> traces = new CopyOnWriteArrayList<>();
    private final List<Metric> metrics = new CopyOnWriteArrayList<>();
    private final List<LogEvent> logs = new CopyOnWriteArrayList<>();
    
    public DefaultTelemetryProvider() {
        this.id = Id.random().asString();
        this.name = "default-telemetry-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Telemetry Provider")
            .version(version)
            .label("type", "telemetry")
            .now()
            .build();
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
    public ResourceType type() { return new ResourceType.Custom("telemetry"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public void trace(TraceEvent event) throws Exception {
        traces.add(event);
    }
    
    @Override
    public void metric(Metric metric) throws Exception {
        metrics.add(metric);
    }
    
    @Override
    public void log(LogEvent event) throws Exception {
        logs.add(event);
        // Also output to console for development
        System.out.println("[" + event.level() + "] " + event.message());
    }
    
    public List<TraceEvent> getTraces() {
        return new ArrayList<>(traces);
    }
    
    public List<Metric> getMetrics() {
        return new ArrayList<>(metrics);
    }
    
    public List<LogEvent> getLogs() {
        return new ArrayList<>(logs);
    }
}