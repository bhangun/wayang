package tech.kayys.wayang.mcp.client.runtime.schema.prompts;

import jakarta.validation.constraints.NotNull;

public class PromptArgument {
    @NotNull
    public String name;
    
    public String description;
    
    public boolean required;
}
