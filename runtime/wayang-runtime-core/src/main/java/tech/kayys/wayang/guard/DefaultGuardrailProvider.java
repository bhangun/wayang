package tech.kayys.wayang.guard;

import java.util.ArrayList;
import java.util.List;

import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.inference.CompletionResult;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultGuardrailProvider implements GuardrailProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final List<String> blockedKeywords = List.of("harmful", "malicious", "illegal");
    
    public DefaultGuardrailProvider() {
        this.id = Id.random().asString();
        this.name = "default-guardrail-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Guardrail Provider")
            .version(version)
            .label("type", "guardrail")
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
    public ResourceType type() { return new ResourceType.Custom("guardrail"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public GuardrailResult validate(ExecutionContext context) throws Exception {
        List<Violation> violations = new ArrayList<>();
        
        // Check input content
        AgentRequest request = context.getVariable("request", AgentRequest.class);
        if (request != null && request.content() != null) {
            String content = request.content().toLowerCase();
            for (String keyword : blockedKeywords) {
                if (content.contains(keyword)) {
                    violations.add(Violation.of(
                        "blocked_keyword",
                        "Blocked keyword detected: " + keyword,
                        "HIGH"
                    ));
                }
            }
        }
        
        // Check completion content
        CompletionResult completion = context.getVariable("completionResult", CompletionResult.class);
        if (completion != null && completion.getContent() != null) {
            String content = completion.getContent().toLowerCase();
            for (String keyword : blockedKeywords) {
                if (content.contains(keyword)) {
                    violations.add(Violation.of(
                        "blocked_keyword",
                        "Blocked keyword in response: " + keyword,
                        "HIGH"
                    ));
                }
            }
        }
        
        if (!violations.isEmpty()) {
            return GuardrailResult.failed("content_filter", "Content validation failed", 
                violations.toArray(new Violation[0]));
        }
        
        return GuardrailResult.passed(0.95);
    }
}