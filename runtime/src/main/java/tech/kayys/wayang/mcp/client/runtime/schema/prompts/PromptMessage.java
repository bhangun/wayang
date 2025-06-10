package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.constraints.NotNull;

public class PromptMessage {
    @NotNull
    public String role; // "user", "assistant", "system"
    
    @NotNull
    public PromptContent content;
}
