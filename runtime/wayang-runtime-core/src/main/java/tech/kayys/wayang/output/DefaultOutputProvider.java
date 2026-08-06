package tech.kayys.wayang.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;


public class DefaultOutputProvider implements OutputProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final List<OutputResult> outputs = new CopyOnWriteArrayList<>();
    
    public DefaultOutputProvider() {
        this.id = Id.random().asString();
        this.name = "default-output-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Output Provider")
            .version(version)
            .label("type", "output")
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
    public ResourceType type() { return new ResourceType.Custom("output"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public void send(OutputResult result) throws Exception {
        outputs.add(result);
        // In production, this would send to the appropriate destination
        System.out.println("Output sent: " + result.content());
    }
    
    @Override
    public Set<OutputType> getSupportedTypes() {
        return Set.of(OutputType.TEXT, OutputType.CONSOLE);
    }
    
    public List<OutputResult> getOutputs() {
        return new ArrayList<>(outputs);
    }
}
