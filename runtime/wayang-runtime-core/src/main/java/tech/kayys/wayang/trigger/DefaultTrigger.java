package tech.kayys.wayang.trigger;

import java.util.Map;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.core.Principal;
import tech.kayys.wayang.event.Event;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.spi.service.ExecutionService;

public class DefaultTrigger implements Trigger {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final TriggerType type;
    private volatile boolean running = false;
    
    public DefaultTrigger(TriggerType type) {
        this.id = Id.random().asString();
        this.name = "default-trigger-" + type.name().toLowerCase();
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Trigger: " + type.name())
            .version(version)
            .label("type", "trigger")
            .label("triggerType", type.name())
            .now()
            .build();
        this.type = type;
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
    public ResourceType type() { return new ResourceType.Custom("trigger"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public TriggerType type() { return type; }
    
    @Override
    public boolean supports(Event event) {
        // Check if the event is relevant
        return true;
    }
    
    @Override
    public void onTrigger(Event event, ExecutionService executionService) throws Exception {
        if (!running) {
            return;
        }
        
        // Create execution context
        ExecutionContext context = ExecutionContext.builder()
            .principal(Principal.system())
            .variable("triggerEvent", event)
            .build();
        
        // Start execution
        executionService.startAgent("default-agent", Map.of("event", event));
    }
    
    @Override
    public void start() throws Exception {
        running = true;
    }
    
    @Override
    public void stop() throws Exception {
        running = false;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
}