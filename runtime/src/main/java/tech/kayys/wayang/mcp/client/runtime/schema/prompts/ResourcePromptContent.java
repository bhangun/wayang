package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.constraints.NotNull;

public class ResourcePromptContent extends PromptContent {
    @NotNull
    public String uri;
    
    public String mimeType;
    
    @Override
    public String type() { return "resource"; }
}
