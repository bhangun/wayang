package tech.kayys.wayang.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.context.ContextProvider;
import tech.kayys.wayang.context.Document;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultContextProvider implements ContextProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    
    public DefaultContextProvider() {
        this.id = Id.random().asString();
        this.name = "default-context-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Context Provider")
            .version(version)
            .label("type", "context")
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
    public ResourceType type() { return new ResourceType.Custom("context"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public ContextData load(ExecutionContext context) throws Exception {
        // Check for existing context data
        ContextData existing = context.getVariable("contextData", ContextData.class);
        if (existing != null) {
            return existing;
        }
        
        // Build context from variables
        List<Document> documents = new ArrayList<>();
        Map<String, Object> structured = new HashMap<>();
        
        // Check for documents in context
        List<Document> docList = context.getVariable("documents", List.class);
        if (docList != null) {
            for (Object doc : docList) {
                if (doc instanceof Document) {
                    documents.add((Document) doc);
                }
            }
        }
        
        // Check for structured data
        Map<String, Object> data = context.getVariable("data", Map.class);
        if (data != null) {
            structured.putAll(data);
        }
        
        return new ContextData(
            Id.random().asString(),
            context.getVariable("query", String.class),
            null,
            documents,
            List.of(),
            List.of(),
            structured,
            Map.of(),
            documents.isEmpty() ? 0.0 : 1.0,
            0,
            "default"
        );
    }
}

