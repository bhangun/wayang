package tech.kayys.wayang.prompt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.context.Document;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.planner.Plan;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultPromptProvider implements PromptProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, String> templates = new ConcurrentHashMap<>();
    
    public DefaultPromptProvider() {
        this.id = Id.random().asString();
        this.name = "default-prompt-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Prompt Provider")
            .version(version)
            .label("type", "prompt")
            .now()
            .build();
        
        // Register default templates
        templates.put("default", """
            You are a helpful assistant.
            
            Context:
            {{#if contextData}}
            {{contextData}}
            {{/if}}
            
            User: {{query}}
            
            Assistant:""");
        
        templates.put("chain-of-thought", """
            Let's think through this step by step.
            
            Question: {{query}}
            
            {{#if contextData}}
            Available information:
            {{contextData}}
            {{/if}}
            
            Let me reason through this:
            1.""");
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
    public ResourceType type() { return new ResourceType.Custom("prompt"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public Prompt build(ExecutionContext context, Plan plan) throws Exception {
        String template = templates.getOrDefault("default", templates.get("default"));
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("query", context.getVariable("query", String.class));
        variables.put("goal", context.getVariable("goal", String.class));
        
        ContextData contextData = context.getVariable("contextData", ContextData.class);
        if (contextData != null) {
            variables.put("contextData", contextData.documents().stream()
                .map(Document::content)
                .collect(Collectors.joining("\n")));
        }
        
        // Simple template rendering (in production, use a proper template engine)
        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            rendered = rendered.replace(placeholder, value);
        }
        
        return Prompt.of(template, variables)
            .withRendered(rendered)
            .withVariable("rendered", rendered);
    }
}
