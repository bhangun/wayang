package tech.kayys.wayang.input;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultInputProvider implements InputProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, InputStreamHandler> handlers = new ConcurrentHashMap<>();
    
    public DefaultInputProvider() {
        this.id = Id.random().asString();
        this.name = "default-input-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Input Provider")
            .version(version)
            .label("type", "input")
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
    public ResourceType type() { return new ResourceType.Custom("input"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public AgentRequest receive(ExecutionContext context) throws Exception {
        // Check if there's a request in the context
        AgentRequest request = context.getVariable("request", AgentRequest.class);
        if (request != null) {
            return request;
        }
        
        // Check for input in variables
        String input = context.getVariable("input", String.class);
        if (input != null) {
            return AgentRequest.of(input);
        }
        
        // Default: empty request
        return AgentRequest.builder()
            .content("")
            .type(InputType.TEXT)
            .build();
    }
    
    @Override
    public Set<InputType> getSupportedTypes() {
        return Set.of(InputType.TEXT, InputType.REST_API);
    }
}