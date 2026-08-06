package tech.kayys.wayang.inference;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultInferenceProvider implements InferenceProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, ModelInfo> models = new ConcurrentHashMap<>();
    
    public DefaultInferenceProvider() {
        this.id = Id.random().asString();
        this.name = "default-inference-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Inference Provider")
            .version(version)
            .label("type", "inference")
            .now()
            .build();
        
        // Register default models
        models.put("default", new ModelInfo("default", "Default Model", "wayang", 
            Set.of("text"), Map.of()));
        models.put("gpt-4", new ModelInfo("gpt-4", "GPT-4", "openai", 
            Set.of("text", "function-calling"), Map.of()));
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
    public ResourceType type() { return new ResourceType.Custom("inference"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public CompletionResult generate(CompletionRequest request) throws Exception {
        // Build a simple response
        StringBuilder response = new StringBuilder();
        
        for (Message message : request.messages()) {
            if (message.role() == MessageRole.USER) {
                response.append("I understand your request: ").append(message.content()).append("\n");
            }
        }
        
        response.append("I will help you with that.");
        
        Message assistantMessage = Message.assistant(response.toString());
        Choice choice = Choice.of(assistantMessage, "stop");
        Usage usage = Usage.of(10, 5);
        
        return CompletionResult.of(choice);
    }
    
    @Override
    public Set<String> listModels() {
        return models.keySet();
    }
    
    @Override
    public ModelInfo getModelInfo(String modelId) {
        return models.getOrDefault(modelId, new ModelInfo(modelId, "Unknown", "unknown", Set.of(), Map.of()));
    }
}