package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public class GetPromptResult {
    public String description;
    
    @NotNull
    public List<PromptMessage> messages;
}
